package de.cubeattack.api.util.versioning;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cubeattack.api.API;
import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.RestAPIUtils;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class VersionUtils {

    public static String getPomVersion(Class<?> clazz) {
        try {
            File pluginDirectory = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile();
            File pomFile = new File(pluginDirectory, "pom.xml");

            LogManager.getLogger().info("Absolute path of pom.xml: " + pomFile.getAbsolutePath()); // Hinzugefügter Code

            BufferedReader bufferedReader = new BufferedReader(new FileReader(pomFile));
            String line;
            Pattern versionPattern = Pattern.compile("<version>(.*?)</version>");

            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = versionPattern.matcher(line);
                if (matcher.find()) {
                    bufferedReader.close();
                    return matcher.group(1);
                }
            }

            bufferedReader.close();
        } catch (IOException | URISyntaxException ex) {
            LogManager.getLogger().warn("Error reading pom.xml file: " + ex.getMessage());
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

    public static VersionUtils.Result checkVersion(String gitHubUser, String repo, String pluginVersion, UpdateSetting updateSetting) {

        Result result = new Result(VersionStatus.FAILED, "UNKNOWN", "UNKNOWN", "UNKNOWN", "NOT FOUND", "");

        try {

            String fileVersion = (latestUpdatedVersion == null ? pluginVersion : latestUpdatedVersion);
            RestAPIUtils restAPIUtils = new RestAPIUtils();
            String url = "https://api.github.com/repos/" + gitHubUser + "/" + repo + "/releases/latest";
            Response response = restAPIUtils.request("GET", url, null);

            if (response == null || response.code() != HttpURLConnection.HTTP_OK) {
                LogManager.getLogger().warn("Plugin (" + fileVersion + ") version check failed '" + response + " (code: " + (response == null ? -1 : response.code()) + ")'");
                //wegen Änderung response.body() nie null somit bedarf für Änderung
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
                    result = new Result(VersionStatus.DEVELOPMENT, pluginVersion, latestVersion, downloadURL, releaseUrl, "");
                    Future<java.lang.String> future = Objects.requireNonNull(updateToLatestVersion(result, updateSetting, 0, null));
                    latestUpdatedVersion = future.get();
                    break;
                }

                case 0: {
                    result = new Result(VersionStatus.LATEST, pluginVersion, latestVersion, downloadURL, releaseUrl, "");
                    break;
                }

                case -1: {
                    result = new Result(VersionStatus.OUTDATED, pluginVersion, latestVersion, downloadURL, releaseUrl, "");
                    Future<java.lang.String> future = Objects.requireNonNull(updateToLatestVersion(result, updateSetting, 0, null));
                    latestUpdatedVersion = future.get();
                }
            }

            if (latestUpdatedVersion != null)
                result = new Result(VersionStatus.REQUIRED_RESTART, pluginVersion, latestVersion, downloadURL, releaseUrl, "");

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


    public static Future<String> updateToLatestVersion(Result result, UpdateSetting updateSetting, int delay, CallbackExecutable callback) {

        String savePath = "./plugins/NeoProtect-" + result.latestVersion + ".jar";

        return API.getExecutorService().schedule(() -> {

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

            LogManager.getLogger().warn("Starting auto-updater for NeoProtect plugin...");

            try {
                LogManager.getLogger().info("Deleting the old plugin version...");
                long deletingTime = AutoUpdater.deleteOldVersion();
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
        }, delay, TimeUnit.SECONDS);
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
                ex.printStackTrace();
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

        private static long deleteOldVersion() {
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

                        if (description.get("name").toString().equalsIgnoreCase("NeoProtect")) {
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
