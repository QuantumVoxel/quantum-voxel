package dev.ultreon.logging.compat;

import dev.ultreon.logging.LogCategory;
import dev.ultreon.logging.LogLevel;
import dev.ultreon.logging.Logger;
import dev.ultreon.logging.LoggerManager;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

class ULogger extends AbstractLogger {
    private static final Logger FAIL_LOGGER = LoggerManager.getLogger("Slf4J:Compat");
    private final Logger logger;

    ULogger(String name) {
        this.logger = LoggerManager.getLogger(name);
        this.name = name;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(LogLevel.WARN);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.WARN);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        FormattingTuple format = format(messagePattern, arguments);
        if (format == null) return;
        if (throwable != null) {
            if (format.getThrowable() != null) {
                throwable.addSuppressed(format.getThrowable());
            }
        } else if (format.getThrowable() != null) {
            throwable = format.getThrowable();
        }
        logger.log(System.currentTimeMillis(), level(level), LogCategory.DEFAULT, format.getMessage(), throwable, false, false);
    }

    private FormattingTuple format(String messagePattern, Object[] arguments) {
        try {
            return MessageFormatter.arrayFormat(messagePattern, arguments);
        } catch (Exception e) {
            FAIL_LOGGER.error(LogCategory.DEFAULT, "Failed to format message! " + e.getMessage());
            return null;
        }
    }

    private LogLevel level(Level level) {
        switch (level) {
            case TRACE:
                return LogLevel.TRACE;
            case DEBUG:
                return LogLevel.DEBUG;
            case WARN:
                return LogLevel.WARN;
            case ERROR:
                return LogLevel.ERROR;
            default:
                return LogLevel.INFO;
        }
    }
}
