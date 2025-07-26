package de.cubeattack.api.util.versioning;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cubeattack.api.API;
import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.JavaUtils;
import de.cubeattack.api.util.RestAPIUtils;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings("unused")
public class VersionUtils {


    public static String getPomVersion(Class<?> clazz) {
        try {
            // Path to the plugin's own JAR file
            URL jarUrl = clazz.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(jarUrl.toURI());

            // Path to the .class file of the given class
            String classPath = clazz.getName().replace('.', '/') + ".class";

            if (!JavaUtils.isRunningAsJarFile(clazz)) {
                LogManager.getLogger().info("Failed to access pom.properties. Not running from a JAR file: " + file.getAbsolutePath());
                return "NOT FOUND (Not running from JAR)";
            }

            if(!file.exists() || !file.canRead()) {
                LogManager.getLogger().warn("Failed to access pom.properties");
                return "NOT FOUND (Can't access file)";
            }

            try (JarFile jarFile = new JarFile(file)) {
                // Ensure that this class is actually inside this JAR
                if (jarFile.getEntry(classPath) == null) {
                    LogManager.getLogger().warn("Class not found inside this JAR: " + classPath);
                    return null;
                }

                // Scan all pom.properties entries in META-INF/maven/
                for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (!name.startsWith("META-INF/maven/") || !name.endsWith("pom.properties"))
                        continue;

                    try (InputStream is = jarFile.getInputStream(entry)) {
                        Properties props = new Properties();
                        props.load(is);

                        String version = props.getProperty("version");
                        if (version != null) {
                            return version;
                        }
                    }
                }

                LogManager.getLogger().warn("No pom.properties found in this JAR");
            }
        } catch (Exception e) {
            LogManager.getLogger().error("Failed to read version from pom.properties: " + e.getMessage(), e);
        }

        return null;
    }

    public static String getBuild() {
        try {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(Paths.get("buildNumber.properties")));
            return properties.getProperty("buildNumber");
        } catch (IOException ex) {
            LogManager.getLogger().error(ex.getLocalizedMessage(), ex);
            LogManager.getLogger().warn("Can't find buildNumber in buildNumber.properties");
        }
        return null;
    }

    private static String latestUpdatedVersion = null;

    public static VersionUtils.Result checkVersion(String pluginName, String pluginVersion, String githubUser, String githubRepo, UpdateSetting updateSetting) {

        Result result = new Result(VersionStatus.FAILED, "UNKNOWN", "UNKNOWN", "UNKNOWN", "NOT FOUND", null);

        try {

            String fileVersion = (latestUpdatedVersion == null ? pluginVersion : latestUpdatedVersion);
            RestAPIUtils restAPIUtils = new RestAPIUtils();
            String url = "https://api.github.com/repos/" + githubUser + "/" + githubRepo + "/releases/latest";
            Response response = restAPIUtils.request("GET", url, null);

            if (response == null || response.code() != HttpURLConnection.HTTP_OK) {
                LogManager.getLogger().warn("Plugin (" + fileVersion + ") version check failed '" + response + " (code: " + (response == null ? -1 : response.code()) + ")'");
                if (response != null)
                    response.body().close();
                return new Result(VersionStatus.FAILED, "ERROR", "ERROR", "ERROR", "NOT FOUND", String.valueOf(response == null ? "REQUEST FAILED (NULL)" : response));
            }

            String json = getBody(response); // deine Methode
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            String latestVersion = jsonObject.get("tag_name").getAsString();
            String releaseUrl = jsonObject.get("html_url").getAsString();
            String downloadURL = jsonObject.getAsJsonArray("assets")
                    .get(0).getAsJsonObject()
                    .get("browser_download_url").getAsString();

            int compareResult = compareVersions((fileVersion.contains(":") ? fileVersion.split(":")[0] : fileVersion), latestVersion);

            switch (compareResult) {

                case 1: {
                    result = new Result(VersionStatus.DEVELOPMENT, pluginVersion, latestVersion, downloadURL, releaseUrl, null);
                    Future<java.lang.String> future = Objects.requireNonNull(updateToLatestVersion(pluginName, result, updateSetting, null));
                    latestUpdatedVersion = future.get();
                    break;
                }

                case 0: {
                    result = new Result(VersionStatus.LATEST, pluginVersion, latestVersion, downloadURL, releaseUrl, null);
                    break;
                }

                case -1: {
                    result = new Result(VersionStatus.OUTDATED, pluginVersion, latestVersion, downloadURL, releaseUrl, null);
                    Future<java.lang.String> future = Objects.requireNonNull(updateToLatestVersion(pluginName, result, updateSetting, null));
                    latestUpdatedVersion = future.get();
                }
            }

            if (latestUpdatedVersion != null)
                result = new Result(VersionStatus.REQUIRED_RESTART, pluginVersion, latestVersion, downloadURL, releaseUrl, null);

        } catch (Exception e) {
            LogManager.getLogger().error("Exception trying to get the latest plugin version", e);
            result = new Result(VersionStatus.FAILED, "UNKNOWN", "UNKNOWN", "UNKNOWN", "NOT FOUND", e.getMessage() == null ? "NO ERROR MESSAGE PROVIDED" : e.getMessage());
        }

        return result;
    }

    private static String getBody(Response response) {
        try (ResponseBody body = response.body()) {
            return body.string();
        } catch (IOException ex) {
            LogManager.getLogger().error(ex.getLocalizedMessage(), ex);
            return "{}";
        }
    }


    public static Future<String> updateToLatestVersion(String pluginName, Result result, UpdateSetting updateSetting, CallbackExecutable callback) {

        String savePath = "./plugins/" + pluginName + "-" + result.latestVersion + ".jar";

        return API.getExecutorService().submit(() -> {

            if (result.latestVersion.equalsIgnoreCase(latestUpdatedVersion)) return latestUpdatedVersion;

            if (result.versionStatus == VersionStatus.LATEST || result.versionStatus == VersionStatus.FAILED) {
                return latestUpdatedVersion;
            }

            if (result.versionStatus == VersionStatus.DEVELOPMENT && !updateSetting.equals(UpdateSetting.ENABLED)) {
                return latestUpdatedVersion;
            }

            if (result.versionStatus == VersionStatus.OUTDATED && updateSetting.equals(UpdateSetting.DISABLED)) {
                return latestUpdatedVersion;
            }

            LogManager.getLogger().warn("Starting auto-updater for " + pluginName + " plugin...");

            try {
                LogManager.getLogger().info("Deleting the old plugin version...");
                long deletingTime = AutoUpdater.deleteOldVersion(pluginName);
                LogManager.getLogger().info("Completed deleting old plugin version! (took " + deletingTime + "ms)");
                LogManager.getLogger().info("Download the latest release " + result.latestVersion + "...");
                long updateTime = AutoUpdater.downloadFile(result.getDownloadUrl(), savePath);
                LogManager.getLogger().info("Update finished! (took " + updateTime + "ms)");
                latestUpdatedVersion = result.latestVersion;

                if (callback != null)
                    callback.run(result.setVersionStatus(VersionStatus.REQUIRED_RESTART));

                return result.latestVersion;
            } catch (IOException ex) {
                LogManager.getLogger().error(ex.getLocalizedMessage(), ex);
            }
            return null;
        });
    }

    private static int compareVersions(String currentVersion, String lastestVersion) {
        ComparableVersion current = new ComparableVersion(currentVersion);
        ComparableVersion latest = new ComparableVersion(lastestVersion);
        return current.compareTo(latest);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static class AutoUpdater {

        private static long downloadFile(String downloadURL, String fileName) throws IOException {
            long startTime = System.currentTimeMillis();

            URL url;
            File file;

            try {
                file = new File(fileName);
                url = new URL(downloadURL);

                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
            } catch (Exception ex) {
                LogManager.getLogger().error(ex.getLocalizedMessage(), ex);
                return -1;
            }

            try (InputStream in = url.openStream();
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                return System.currentTimeMillis() - startTime;
            }
        }

        private static long deleteOldVersion(String pluginName) {
            long startTime = System.currentTimeMillis();

            for (File file : Objects.requireNonNull(new File("./plugins/").listFiles())) {

                if (file.isDirectory()) {
                    continue;
                }

                try (JarFile jar = new JarFile(file)) {
                    JarEntry entry = jar.getJarEntry("plugin.yml");

                    if (entry == null) {
                        continue;
                    }

                    try (InputStream in = jar.getInputStream(entry)) {
                        HashMap<String, Object> description = new Yaml().load(in);

                        if (description.get("name").toString().equalsIgnoreCase(pluginName)) {
                            if (!file.exists()) return -2;
                            if (!file.delete()) return -1;
                            return System.currentTimeMillis() - startTime;
                        }
                    }
                } catch (IOException ex) {
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }
            }
            return Integer.MIN_VALUE;
        }
    }

    public static class Result {

        private VersionStatus versionStatus;
        private final String currentVersion;
        private final String latestVersion;
        private final String downloadUrl;
        private final String releaseUrl;
        private final String error;

        public Result(VersionStatus versionStatus, String currentVersion, String latestVersion, String downloadUrl, String releaseUrl, String error) {
            this.versionStatus = versionStatus;
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
            this.downloadUrl = downloadUrl;
            this.releaseUrl = releaseUrl;
            this.error = error;
        }

        public Result message() {
            if (versionStatus == VersionStatus.DEVELOPMENT) {
                LogManager.getLogger().error("Plugin is on development version (" + currentVersion + ")");
            } else if (versionStatus == VersionStatus.LATEST) {
                LogManager.getLogger().info("Plugin is up to date (" + currentVersion + ")");
            } else if (versionStatus == VersionStatus.REQUIRED_RESTART) {
                LogManager.getLogger().warn("Plugin is outdated (" + currentVersion + ") and requires a restart");
                LogManager.getLogger().warn("Current version: " + currentVersion);
                LogManager.getLogger().warn("Version after restart: " + latestVersion);
            } else {
                LogManager.getLogger().warn("Plugin is outdated (" + currentVersion + ")");
                LogManager.getLogger().warn("Latest version: " + latestVersion);
                LogManager.getLogger().warn("Release URL: " + releaseUrl);
            }
            return this;
        }

        public Result setVersionStatus(VersionStatus versionStatus) {
            this.versionStatus = versionStatus;
            return this;
        }

        public VersionStatus getVersionStatus() {
            return versionStatus;
        }

        public String getCurrentVersion() {
            return currentVersion;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getReleaseUrl() {
            return releaseUrl;
        }

        public String getError() {
            return error;
        }
    }

    public enum VersionStatus {
        FAILED,
        LATEST,
        OUTDATED,
        DEVELOPMENT,
        REQUIRED_RESTART
    }

    public enum UpdateSetting {
        DEV,
        ENABLED,
        DISABLED;

        public static UpdateSetting getByNameOrDefault(String value) {
            for (UpdateSetting setting : UpdateSetting.values()) {
                if (setting.name().equalsIgnoreCase(value)) {
                    return setting;
                }
            }
            return UpdateSetting.ENABLED;
        }
    }
}
