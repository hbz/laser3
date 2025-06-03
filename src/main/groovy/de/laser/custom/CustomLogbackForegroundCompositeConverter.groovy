package de.laser.custom

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase
import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

class CustomLogbackForegroundCompositeConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel()
        switch (level.toInt()) {
            case Level.ERROR_INT:
                return BOLD + RED_FG
            case Level.WARN_INT:
                return BOLD + YELLOW_FG
            case Level.INFO_INT:
                return CYAN_FG
            default:
                return DEFAULT_FG
        }
    }
}
