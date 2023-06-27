package de.cubeattack.api.logger;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Arrays;

public class CustomClassOfCallerConverter extends ClassOfCallerConverter {
    @Override
    public String convert(ILoggingEvent event) {
        StackTraceElement[] stackTrace = event.getCallerData();
        if (stackTrace != null && stackTrace.length > 0) {
            StackTraceElement element = Arrays.asList(stackTrace).get(stackTrace.length-event.getCallerData().length == 2 ? 1 : 2);
            return element.getClassName() + ":" + element.getMethodName();
        }
        return super.convert(event);
    }
}