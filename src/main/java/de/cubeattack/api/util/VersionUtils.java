package de.cubeattack.api.util;

import de.cubeattack.api.logger.LogManager;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
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

    public static String getBuild(){
        try {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(Paths.get("buildNumber.properties")));
            return properties.getProperty("buildNumber");
        } catch (IOException ex) {
            ex.printStackTrace();
            LogManager.getLogger().warn("Can't find buildNumber in buildNumber.properties");
        }
        return null;
    }

    public static @NotNull VersionUtils.Result checkVersion(String gitHubUser, String repo, String currentVersion) {

        try {
            URL url = new URL("https://api.github.com/repos/" + gitHubUser + "/" + repo + "/releases/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String latestVersion = jsonResponse.getString("tag_name");
                String releaseUrl = jsonResponse.getString("html_url");

                int compareResult = compareVersions((currentVersion.contains(":") ? currentVersion.split(":")[0] : currentVersion), latestVersion);

                if(compareResult > 0) {
                    return new Result(VersionStatus.DEVELOPMENT, currentVersion, latestVersion, releaseUrl);
                } else if (compareResult == 0) {
                    return new Result(VersionStatus.LATEST, currentVersion, latestVersion, releaseUrl);
                }else {
                    return new Result(VersionStatus.OUTDATED, currentVersion, latestVersion, releaseUrl);
                }
            }else {
                LogManager.getLogger().warn("Version check failed '" + connection.getResponseMessage() + " (code: " + connection.getResponseCode() + ")'");
            }
        }catch (UnknownHostException | SocketTimeoutException | SocketException connectionException){
            LogManager.getLogger().error(connectionException.getMessage());
        }
        catch (Exception e) {
            LogManager.getLogger().error("Exception trying to get the latest plugin version", e);
        }
        return new Result(VersionStatus.LATEST,null, null, null);
    }

    private static int compareVersions(String currentVersion, String lastestVersion) {
        ComparableVersion current = new ComparableVersion(currentVersion);
        ComparableVersion latest = new ComparableVersion(lastestVersion);
        return current.compareTo(latest);
    }

    public static class Result {
        private final VersionStatus versionStatus;
        private final String currentVersion;
        private final String latestVersion;
        private final String releaseUrl;

        public Result(VersionStatus versionStatus, String currentVersion, String latestVersion, String releaseUrl) {
            this.versionStatus = versionStatus;
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
            this.releaseUrl = releaseUrl;
        }

        public Result message(){
            if(versionStatus == VersionStatus.DEVELOPMENT) {
                LogManager.getLogger().error("Plugin is on development version (" + currentVersion + ")");
            } else if (versionStatus == VersionStatus.LATEST) {
                LogManager.getLogger().info("Plugin is up to date (" + currentVersion + ")");
            }else {
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
    }

    public enum VersionStatus {
        LATEST,
        OUTDATED,
        DEVELOPMENT
    }
}
