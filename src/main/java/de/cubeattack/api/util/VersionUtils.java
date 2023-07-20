package de.cubeattack.api.util;

import de.cubeattack.api.API;
import de.cubeattack.api.logger.LogManager;
import okhttp3.Response;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Future;
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

    private static String lastestUpdatedVersion = null;

    public static @NotNull VersionUtils.Result checkVersion(String gitHubUser, String repo, String currentVersion, boolean autoUpdate) {

        RestAPIUtils restAPIUtils = new RestAPIUtils();
        String url = "https://api.github.com/repos/" + gitHubUser + "/" + repo + "/releases/latest";

        try (Response response = restAPIUtils.request("GET", url, null)) {

            int responseCode = response.code();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                JSONObject jsonResponse = new JSONObject(response.body().string());
                String downloadURL = jsonResponse.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                String latestVersion = jsonResponse.getString("tag_name");
                String releaseUrl = jsonResponse.getString("html_url");

                int compareResult = compareVersions((currentVersion.contains(":") ? currentVersion.split(":")[0] : currentVersion), latestVersion);

                if (compareResult > 0) {
                    return new Result(VersionStatus.DEVELOPMENT, currentVersion, latestVersion, releaseUrl);
                } else if (compareResult == 0) {
                    return new Result(VersionStatus.LATEST, currentVersion, latestVersion, releaseUrl);
                } else {
                    long start = System.currentTimeMillis();
                    if (autoUpdate && !lastestUpdatedVersion.equalsIgnoreCase(latestVersion)) {
                        lastestUpdatedVersion = updateToLatestVersion(downloadURL, "./plugins/NeoPlugin-" + latestVersion + ".jar", latestVersion).get();
                        System.out.println(System.currentTimeMillis() - start);
                        return new Result(VersionStatus.REQUIRED_RESTART, currentVersion, latestVersion, releaseUrl);
                    }

                    return new Result(VersionStatus.OUTDATED, currentVersion, latestVersion, releaseUrl);
                }
            } else {
                LogManager.getLogger().warn("Version check failed '" + response + " (code: " + response.code() + ")'");
            }
        } catch (Exception e) {
            LogManager.getLogger().error("Exception trying to get the latest plugin version", e);
        }
        return new Result(VersionStatus.LATEST,null, null, null);
    }

    public static Future<String> updateToLatestVersion(String urlString, String savePath, String latestVersion) {

        return API.getExecutorService().submit(() -> {
            if(latestVersion.equalsIgnoreCase(lastestUpdatedVersion))return latestVersion;

            System.out.println(urlString);

            try  {
                System.out.println("Deleting the old plugin version...");
                long deletingTime =  FileScanner.deleteOldVersion();
                System.out.println("Completed deleting old plugin version! (took " + deletingTime + ")");
                System.out.println("Download the latest version " + latestVersion + "...");
                long updateTime = AutoUpdater.downloadFile(urlString, savePath);
                System.out.println("Update finished! (took " + updateTime + ")");
                return latestVersion;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
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
        DEVELOPMENT,
        REQUIRED_RESTART
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static class AutoUpdater {

        public static long downloadFile(String fileURL, String fileName) throws IOException {
            long startTime = System.currentTimeMillis();
            File file = new File(fileName);
            URL url = new URL(fileURL);

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
    }

    public static class FileScanner {
        public static long deleteOldVersion() {

            long startTime = System.currentTimeMillis();

            File folder = new File("plugins");

            for (File file : Objects.requireNonNull(folder.listFiles())) {
                try (JarFile jarFile = new JarFile(file.getAbsoluteFile())) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();

                        if (entryName.equals("plugin.yml")) {

                            try (URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL("file:/" + file.getAbsoluteFile())})) {
                                Scanner sc = new Scanner(Objects.requireNonNull(classLoader.getResourceAsStream("plugin.yml")));
                                while (sc.hasNextLine()) {
                                    if (sc.nextLine().equals("name: NeoProtect")) {

                                        jarFile.close();
                                        classLoader.close();

                                        System.err.println("delete");
                                        System.out.println(file.getName());
                                        System.out.println(file.delete());

                                        return System.currentTimeMillis() - startTime;
                                    }
                                }
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return System.currentTimeMillis() - startTime;
        }
    }
}
