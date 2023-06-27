package de.cubeattack.api.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.cubeattack.api.utils.JavaUtils;
import org.slf4j.LoggerFactory;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

@SuppressWarnings("unused")
public class LogManager
{

    static {
        if(JavaUtils.javaVersionCheck() < 11){
            System.setProperty("logback.configurationFile", "src/main/resources/logback-8.xml");
        }else {
            System.setProperty("logback.configurationFile", "src/main/resources/logback-17.xml");
        }
    }

    private static final LogManager logManager = new LogManager();
    public Object logger = LoggerFactory.getLogger("de.cubeattack");


    public LogManager setLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
        return this;
    }
    public LogManager setLevel(Object level) {
        return setLevel(level, logger);
    }

    public LogManager setLevel(Object level, Object logger) {
        if(logger instanceof Logger && level instanceof Level) ((Logger) logger).setLevel((Level) level);
        if(logger instanceof java.util.logging.Logger && level instanceof java.util.logging.Level) ((java.util.logging.Logger) logger).setLevel((java.util.logging.Level) level);
        return this;
    }

    public void trace(String output){
        if(logger instanceof Logger) ((Logger) logger).trace(output);
        if(logger instanceof java.util.logging.Logger) ((java.util.logging.Logger) logger).log(java.util.logging.Level.FINE, output);
    }
    public void debug(String output){
        if(logger instanceof Logger) ((Logger) logger).debug(output);
        if(logger instanceof java.util.logging.Logger) ((java.util.logging.Logger) logger).log(java.util.logging.Level.CONFIG, output);
    }

    public void info(String output){
        if(logger instanceof Logger) ((Logger) logger).info(output);
        if(logger instanceof java.util.logging.Logger) ((java.util.logging.Logger) logger).log(java.util.logging.Level.INFO, output);
    }
    public void warn(String output){
        if(logger instanceof Logger) ((Logger) logger).warn(output);
        if(logger instanceof java.util.logging.Logger) ((java.util.logging.Logger) logger).log(java.util.logging.Level.WARNING, output);
    }
    public void error(String output){
        if(logger instanceof Logger) ((Logger) logger).error(output);
        if(logger instanceof java.util.logging.Logger) ((java.util.logging.Logger) logger).log(java.util.logging.Level.SEVERE, output);
    }

    public void loggerTransfer() {
        java.util.logging.Logger javaLogger = java.util.logging.Logger.getLogger("");

        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                Level level;
                if (record.getLevel() == java.util.logging.Level.SEVERE) {
                    level = Level.ERROR;
                } else if (record.getLevel() == java.util.logging.Level.WARNING) {
                    level = Level.WARN;
                } else if (record.getLevel() == java.util.logging.Level.INFO) {
                    level = Level.INFO;
                } else {
                    level = Level.DEBUG;
                }

                if(!(logger instanceof Logger))return;
                ((Logger)logger).log(null, Logger.FQCN, level.toInt()/1000, record.getMessage(), null, record.getThrown());
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {}
        };

        for (Handler existingHandler : javaLogger.getHandlers()) {
            javaLogger.removeHandler(existingHandler);
        }

        javaLogger.addHandler(handler);
    }

    public static LogManager getLogger() {
        return logManager;
    }
}

