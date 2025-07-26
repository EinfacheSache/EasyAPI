package de.cubeattack.api.util;

public class JavaUtils {

    public static int javaVersionCheck() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static boolean isRunningAsJarFile() {
        return JavaUtils.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath()
                .endsWith(".jar");
    }
}
