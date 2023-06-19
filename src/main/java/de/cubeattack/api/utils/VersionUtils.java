package de.cubeattack.api.utils;

import de.cubeattack.api.logger.LogManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class VersionUtils {

    public static String getVersion() {
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
}
