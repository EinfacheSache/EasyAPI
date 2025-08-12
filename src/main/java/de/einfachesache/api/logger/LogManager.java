package de.einfachesache.api.logger;

import de.einfachesache.api.util.JavaUtils;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class LogManager {

    static {
        if (JavaUtils.javaVersionCheck() < 11) {
            System.setProperty("logback.configurationFile", "src/main/resources/logback-8.xml");
        } else {
            System.setProperty("logback.configurationFile", "src/main/resources/logback-17.xml");
        }
    }

    private static final LogManager logManager = new LogManager();
    public Object logger = Logger.getLogger("de.einfachesache");

    public LogManager setLogger(Object logger) {
        this.logger = logger;
        return this;
    }

    public LogManager setLevel(Object level) {
        return setLevel(level, logger);
    }

    public LogManager setLevel(Object level, Object logger) {
        if (logger instanceof java.util.logging.Logger && level instanceof java.util.logging.Level) {
            ((java.util.logging.Logger) logger).setLevel((java.util.logging.Level) level);
            return this;
        }
        if (logger instanceof ch.qos.logback.classic.Logger && level instanceof ch.qos.logback.classic.Level)
            ((ch.qos.logback.classic.Logger) logger).setLevel((ch.qos.logback.classic.Level) level);
        return this;
    }

    public void trace(String output) {
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).log(java.util.logging.Level.FINE, output);
        } else if (logger instanceof org.slf4j.Logger) ((org.slf4j.Logger) logger).trace(output);
    }

    public void debug(String output) {
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).log(java.util.logging.Level.CONFIG, output);
        } else if (logger instanceof org.slf4j.Logger) ((org.slf4j.Logger) logger).debug(output);
    }

    public void info(String output) {
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).log(java.util.logging.Level.INFO, output);
        } else if (logger instanceof org.slf4j.Logger) ((org.slf4j.Logger) logger).info(output);
    }

    public void warn(String output) {
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).log(java.util.logging.Level.WARNING, output);
        } else if (logger instanceof org.slf4j.Logger) ((org.slf4j.Logger) logger).warn(output);
    }

    public void error(String output) {
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).log(java.util.logging.Level.SEVERE, output);
        } else if (logger instanceof org.slf4j.Logger) ((org.slf4j.Logger) logger).error(output);
    }

    public void error(String output, Throwable throwable) {
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).log(java.util.logging.Level.SEVERE, output, throwable);
        } else if (logger instanceof org.slf4j.Logger) ((org.slf4j.Logger) logger).error(output, throwable);
    }

    public void loggerTransfer() {
        java.util.logging.Logger javaLogger = java.util.logging.Logger.getLogger("");

        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                ch.qos.logback.classic.Level level;
                if (record.getLevel() == java.util.logging.Level.SEVERE) {
                    level = ch.qos.logback.classic.Level.ERROR;
                } else if (record.getLevel() == java.util.logging.Level.WARNING) {
                    level = ch.qos.logback.classic.Level.WARN;
                } else if (record.getLevel() == java.util.logging.Level.INFO) {
                    level = ch.qos.logback.classic.Level.INFO;
                } else {
                    level = ch.qos.logback.classic.Level.DEBUG;
                }

                if (!(logger instanceof ch.qos.logback.classic.Logger)) return;
                ((ch.qos.logback.classic.Logger) logger).log(null, ch.qos.logback.classic.Logger.FQCN, level.toInt() / 1000, record.getMessage(), null, record.getThrown());
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

