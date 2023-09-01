package de.cubeattack.api.util.versioning;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import de.cubeattack.api.API;
import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.RestAPIUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
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

    public static @NotNull VersionUtils.Result checkVersion(String gitHubUser, String repo, String pluginVersion, UpdateSetting autoUpdate, int delay) {

        Result result = new Result(VersionStatus.FAILED, "UNKNOWN", "UNKNOWN", "NOT FOUND", "");

        try {

            String fileVersion = (latestUpdatedVersion == null ? pluginVersion : latestUpdatedVersion);
            RestAPIUtils restAPIUtils = new RestAPIUtils();
            String url = "https://api.github.com/repos/" + gitHubUser + "/" + repo + "/releases/latest";
            Response response = restAPIUtils.request("GET", url, null);

            if (response == null || response.code() != HttpURLConnection.HTTP_OK) {
                LogManager.getLogger().warn("Plugin (" + fileVersion + ") version check failed '" + response + " (code: " + (response == null ? -1 : response.code()) + ")'");
                if (response != null && response.body() != null)
                    response.body().close();
                return new Result(VersionStatus.FAILED, "ERROR", "ERROR", "NOT FOUND", String.valueOf(response));
            }

            JSONObject jsonResponse = new JSONObject(getBody(response));
            String downloadURL = jsonResponse.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
            String latestVersion = jsonResponse.getString("tag_name");
            String releaseUrl = jsonResponse.getString("html_url");

            int compareResult = compareVersions((fileVersion.contains(":") ? fileVersion.split(":")[0] : fileVersion), latestVersion);

            switch (compareResult) {

                case 1: {
                    if (autoUpdate.equals(UpdateSetting.ENABLED)) {
                        Future<java.lang.String> future = Objects.requireNonNull(updateToLatestVersion(downloadURL, "./plugins/NeoProtect-" + latestVersion + ".jar", latestVersion, delay));
                        latestUpdatedVersion = delay == 0 ? future.get() : null;
                    }
                    result = new Result(VersionStatus.DEVELOPMENT, pluginVersion, latestVersion, releaseUrl, "");
                    break;
                }

                case 0: {
                    result = new Result(VersionStatus.LATEST, pluginVersion, latestVersion, releaseUrl, "");
                    break;
                }

                case -1: {
                    if (!autoUpdate.equals(UpdateSetting.DISABLED)) {
                        Future<java.lang.String> future = Objects.requireNonNull(updateToLatestVersion(downloadURL, "./plugins/NeoProtect-" + latestVersion + ".jar", latestVersion, delay));
                        latestUpdatedVersion = delay == 0 ? future.get() : null;
                    }
                    result = new Result(VersionStatus.OUTDATED, pluginVersion, latestVersion, releaseUrl, "");
                }
            }

            if (latestUpdatedVersion != null)
                result = new Result(VersionStatus.REQUIRED_RESTART, pluginVersion, latestVersion, releaseUrl, "");

        } catch (Exception e) {
            LogManager.getLogger().error("Exception trying to get the latest plugin version", e);
            result = new Result(VersionStatus.FAILED, "UNKNOWN", "UNKNOWN", "NOT FOUND", e.getMessage());
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


    public static Future<String> updateToLatestVersion(String downloadURL, String savePath, String latestRelease, int delay) {
        return API.getExecutorService().schedule(() -> {

            if (latestRelease.equalsIgnoreCase(latestUpdatedVersion)) return latestRelease;

            LogManager.getLogger().warn("Starting auto-updater for NeoProtect plugin...");

            try {
                LogManager.getLogger().info("Deleting the old plugin version...");
                long deletingTime = AutoUpdater.deleteOldVersion();
                LogManager.getLogger().info("Completed deleting old plugin version! (took " + deletingTime + "ms)");
                LogManager.getLogger().info("Download the latest release " + latestRelease + "...");
                long updateTime = AutoUpdater.downloadFile(downloadURL, savePath);
                LogManager.getLogger().info("Update finished! (took " + updateTime + "ms)");
                latestUpdatedVersion = latestRelease;
                return latestRelease;
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

        public static long downloadFile(String downloadURL, String fileName) throws IOException {
            long startTime = System.currentTimeMillis();
            File file = new File(fileName);
            URL url = new URL(downloadURL);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
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

        public static long deleteOldVersion() {
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
        private final VersionStatus versionStatus;
        private final String currentVersion;
        private final String latestVersion;
        private final String releaseUrl;
        private final String error;

        public Result(VersionStatus versionStatus, String currentVersion, String latestVersion, String releaseUrl, String error) {
            this.versionStatus = versionStatus;
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
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

        public VersionStatus getVersionStatus() {
            return versionStatus;
        }

        public String getCurrentVersion() {
            return currentVersion;
        }

        public String getLatestVersion() {
            return latestVersion;
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
