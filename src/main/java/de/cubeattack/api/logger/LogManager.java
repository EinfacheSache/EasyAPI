package de.cubeattack.api.logger;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class LogManager
{

    static {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        }
        if(Integer.parseInt(version) < 11){
            System.setProperty("logback.configurationFile", "src/main/resources/logback-8.xml");
        }else {
            System.setProperty("logback.configurationFile", "src/main/resources/logback-17.xml");
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("de.cubeattack");

    public static void setLevel(Level level) {
        ((ch.qos.logback.classic.Logger) LOGGER).setLevel(level);
    }

    public static void setLevel(Level level, String logger) {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(logger)).setLevel(level);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
