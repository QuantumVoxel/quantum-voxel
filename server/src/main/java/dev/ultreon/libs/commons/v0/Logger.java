package dev.ultreon.libs.commons.v0;

@FunctionalInterface
public interface Logger {
    void log(Level level, String message, Throwable t);

    default void log(String message) {
        this.log(Level.DEBUG, message, null);
    }

    default void debug(String message) {
        this.log(Level.DEBUG, message, null);
    }

    default void info(String message) {
        this.log(Level.INFO, message, null);
    }

    default void warn(String message) {
        this.log(Level.WARN, message, null);
    }

    default void error(String message) {
        this.log(Level.ERROR, message, null);
    }

    default void log(String message, Throwable t) {
        this.log(Level.DEBUG, message, t);
    }

    default void debug(String message, Throwable t) {
        this.log(Level.DEBUG, message, t);
    }

    default void info(String message, Throwable t) {
        this.log(Level.INFO, message, t);
    }

    default void warn(String message, Throwable t) {
        this.log(Level.WARN, message, t);
    }

    default void error(String message, Throwable t) {
        this.log(Level.ERROR, message, t);
    }

    enum Level {
        DEBUG, INFO, WARN, ERROR
    }
}
