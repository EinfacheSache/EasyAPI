package de.cubeattack.api.utils;

import de.cubeattack.api.logger.LogManager;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class VersionUtils {

    public static String getPomVersion() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("pom.xml"));
            String line;
            Pattern versionPattern = Pattern.compile("<version>(.*?)</version>");

            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = versionPattern.matcher(line);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

            bufferedReader.close();
        } catch (IOException ex) {
            LogManager.getLogger().warn("Can't find version in pom.xml");
        }
        return null;
    }

    public static String getBuild(){
        try {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(Paths.get("buildNumber.properties")));
            return properties.getProperty("buildNumber");
        } catch (IOException ex) {
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

                int compareResult = compareVersions(currentVersion, latestVersion);

                if(compareResult > 0) {
                    LogManager.getLogger().error("Plugin is on development version (" + currentVersion + ")");
                    return new Result(VersionStatus.DEVELOPMENT, latestVersion, releaseUrl);
                } else if (compareResult == 0) {
                    LogManager.getLogger().info("Plugin is up to date (" + currentVersion + ")");
                    return new Result(VersionStatus.LATEST, null, null);

                }else {
                    LogManager.getLogger().warn("Plugin is outdated (" + currentVersion + ")");
                    LogManager.getLogger().warn("Latest version: " + latestVersion);
                    LogManager.getLogger().warn("Release URL: " + releaseUrl);
                    return new Result(VersionStatus.OUTDATED, latestVersion, releaseUrl);
                }
            }
        } catch (Exception e) {
            LogManager.getLogger().error("Exception trying to get the latest plugin version", e);
        }
        return new Result(VersionStatus.LATEST, null, null);
    }

    private static int compareVersions(String currentVersion, String lastestVersion) {
        ComparableVersion current = new ComparableVersion(currentVersion);
        ComparableVersion latest = new ComparableVersion(lastestVersion);
        return current.compareTo(latest);
    }

    public static class Result {
        private final VersionStatus versionStatus;
        private final String latestVersion;
        private final String releaseUrl;

        public Result(VersionStatus versionStatus, String latestVersion, String releaseUrl) {
            this.versionStatus = versionStatus;
            this.latestVersion = latestVersion;
            this.releaseUrl = releaseUrl;
        }

        public VersionStatus getVersionStatus() {
            return versionStatus;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public String getReleaseUrl() {
            return releaseUrl;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof VersionStatus))return false;
            return versionStatus.equals(obj);
        }
    }


    public enum VersionStatus {
        LATEST,
        OUTDATED,
        DEVELOPMENT;
    }
}
