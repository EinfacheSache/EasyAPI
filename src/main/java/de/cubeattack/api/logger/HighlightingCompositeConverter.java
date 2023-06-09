package de.cubeattack.api.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

public class HighlightingCompositeConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        switch (level.toInt()) {
            case Level.ERROR_INT : return RED_FG;
            case Level.WARN_INT : return YELLOW_FG;
            case Level.INFO_INT : return BLUE_FG;
            case Level.DEBUG_INT : return GREEN_FG;
            case Level.TRACE_INT : return CYAN_FG;
            default : throw new IllegalStateException("Unexpected value: " + level.toInt());
        }
    }
}
