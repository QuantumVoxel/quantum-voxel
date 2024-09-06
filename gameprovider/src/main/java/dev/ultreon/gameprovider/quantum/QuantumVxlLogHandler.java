package dev.ultreon.gameprovider.quantum;

import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import net.fabricmc.loader.impl.util.log.LogLevel;
import org.apache.logging.log4j.*;

import java.util.HashMap;
import java.util.Map;

/**
 * QuantumVxlLogHandler class that implements LogHandler interface.
 * Responsible for handling logging operations.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class QuantumVxlLogHandler implements LogHandler {

    // Logger instance for logging
    private static final Logger LOGGER = LogManager.getLogger("FabricLoader");

    // Map to store log categories and their respective markers
    private final Map<LogCategory, Marker> markerMap = new HashMap<>();

    /**
     * Log a message with the specified level, category, message, exception, and other flags.
     * @param time The time of the log event
     * @param level The log level
     * @param category The category of the log
     * @param msg The message to log
     * @param exc The exception to log
     * @param fromReplay Flag indicating if log is from a replay
     * @param wasSuppressed Flag indicating if log was suppressed
     */
    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay, boolean wasSuppressed) {
        // Compute or get the marker for the log category
        Marker marker = this.markerMap.computeIfAbsent(category, logCategory -> MarkerManager.getMarker(logCategory.name));
        // Log the message with the appropriate level and marker
        QuantumVxlLogHandler.LOGGER.log(QuantumVxlLogHandler.getLevel(level), marker, msg, exc);
    }

    /**
     * Check if logging is enabled for the specified level and category.
     * @param level The log level
     * @param category The log category
     * @return True if logging is enabled, false otherwise
     */
    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        return QuantumVxlLogHandler.LOGGER.isEnabled(QuantumVxlLogHandler.getLevel(level));
    }

    /**
     * Get the corresponding Level object for the given LogLevel.
     * @param level The LogLevel enum
     * @return The corresponding Level object
     */
    private static Level getLevel(LogLevel level) {
        switch (level) {
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case DEBUG:
                return Level.DEBUG;
            case ERROR:
                return Level.ERROR;
            case TRACE:
                return Level.TRACE;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Close method for any necessary cleanup operations.
     */
    @Override
    public void close() {
        // No cleanup needed at the moment
    }
}
