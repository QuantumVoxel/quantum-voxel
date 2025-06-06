package dev.ultreon.gameprovider.quantum;

import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import net.fabricmc.loader.impl.util.log.LogLevel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.ultreon.gameprovider.quantum.AnsiColors.*;

/**
 * QuantumVxlLogHandler class that implements LogHandler interface.
 * Responsible for handling logging operations.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class QuantumVxlLogHandler implements LogHandler {


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
        StringBuilder sb = new StringBuilder();
        sb.append(RESET + "[").append(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_TIME)).append("] ").append(colorFor(level)).append("[").append(level).append("] ").append(CYAN).append("[").append(category).append("] ").append(RESET).append(msg);
        if (wasSuppressed) {
            sb.append(RED + " (suppressed)" + RESET);
        }
        if (fromReplay) {
            sb.append(RED + " (replay)" + RESET);
        }
        sb.append(RESET + "\n");
        if (exc == null) {
            System.out.println(sb);
            return;
        }
        sb.append(exc.getClass().getName()).append(": ").append(exc.getMessage()).append("\n");
        for (StackTraceElement ste : exc.getStackTrace()) {
            sb.append("    at ").append(CYAN).append(ste.getClassName()).append(RESET).append(".").append(YELLOW).append(ste.getMethodName()).append("(").append(PURPLE).append(ste.getFileName() == null ? RED + "Unknown source" : ste.getLineNumber()).append(RESET).append(":").append(PURPLE).append(ste.getLineNumber() == 0 ? RED + "???" : ste.getLineNumber()).append(RESET).append(")").append("\n");
        }

        sb.append("\n");

        System.out.print(sb);
    }

    private String colorFor(LogLevel level) {
        switch (level) {
            case INFO:
                return GREEN;
            case TRACE:
                return WHITE;
            case DEBUG:
                return BLUE;
            case WARN:
                return YELLOW;
            case ERROR:
                return RED;
            default:
               throw new IllegalArgumentException();
        }
    }

    /**
     * Check if logging is enabled for the specified level and category.
     * @param level The log level
     * @param category The log category
     * @return True if logging is enabled, false otherwise
     */
    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        return true;
    }

    /**
     * Get the corresponding Level object for the given LogLevel.
     * @param level The LogLevel enum
     * @return The corresponding Level object
     */
    private static Level getLevel(LogLevel level) {
        switch (level) {
            case INFO:
            case TRACE:
            case DEBUG:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            case ERROR:
                return Level.SEVERE;
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
