package dev.ultreon.quantum;

public interface Logger {
    enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
    
    default void log(Level level, String message, Object[] args, Throwable t) {
        try {
            log(level, format(message, args), t);
        } catch (Exception e) {
            log(Level.ERROR, "Failed to format log message: " + message, e);
        }
    }

    static String format(String message, Object[] args) {
        if (message == null) return null;
        if (args == null || args.length == 0) return message;

        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int startIndex = 0;
        int placeholderIndex;

        while ((placeholderIndex = message.indexOf("{}", startIndex)) != -1 && argIndex < args.length) {
            result.append(message, startIndex, placeholderIndex);
            result.append(args[argIndex++]);
            startIndex = placeholderIndex + 2;
        }

        result.append(message.substring(startIndex));
        return result.toString();
    }

    default void log(Level level, String message, Object... args) {
        if (args[args.length - 1] instanceof Throwable) {
            Throwable t = (Throwable) args[args.length - 1];
            Object[] newArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, newArgs, 0, newArgs.length);
            log(level, message, newArgs, t);
            return;
        }
        log(level, message, args, null);
    }
    
    void log(Level level, String message, Throwable t);

    default void log(Level level, String message) {
        log(level, message, new Object[] {}, null);
    }
    
    default void trace(String message) {
        log(Level.TRACE, message);
    }

    default void trace(String message, Throwable t) {
        log(Level.TRACE, message, t);
    }

    default void trace(String message, Object... args) {
        log(Level.TRACE, message, args);
    }

    default void trace(String message, Throwable t, Object... args) {
        log(Level.TRACE, message, args, t);
    }

    default void trace(Throwable t, Object... args) {
        log(Level.TRACE, "", args, t);
    }

    default void debug(String message) {
        log(Level.DEBUG, message);
    }

    default void debug(String message, Throwable t) {
        log(Level.DEBUG, message, t);
    }

    default void debug(String message, Object... args) {
        log(Level.DEBUG, message, args);
    }

    default void debug(String message, Throwable t, Object... args) {
        log(Level.DEBUG, message, args, t);
    }

    default void debug(Throwable t, Object... args) {
        log(Level.DEBUG, "", args, t);
    }

    default void info(String message) {
        log(Level.INFO, message);
    }

    default void info(String message, Throwable t) {
        log(Level.INFO, message, t);
    }

    default void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    default void info(String message, Throwable t, Object... args) {
        log(Level.INFO, message, args, t);
    }

    default void info(Throwable t, Object... args) {
        log(Level.INFO, "", args, t);
    }

    default void warn(String message) {
        log(Level.WARN, message);
    }

    default void warn(String message, Throwable t) {
        log(Level.WARN, message, t);
    }

    default void warn(String message, Object... args) {
        log(Level.WARN, message, args);
    }

    default void warn(String message, Throwable t, Object... args) {
        log(Level.WARN, message, args, t);
    }

    default void warn(Throwable t, Object... args) {
        log(Level.WARN, "", args, t);
    }

    default void error(String message) {
        log(Level.ERROR, message);
    }
    
    default void error(String message, Throwable t) {
        log(Level.ERROR, message, t);
    }
    
    default void error(Throwable t) {
        log(Level.ERROR, "", t);
    }
    
    default void error(String message, Object... args) {
        log(Level.ERROR, message, args);
    }
    
    default void error(String message, Throwable t, Object... args) {
        log(Level.ERROR, message, args, t);
    }
    
    default void error(Throwable t, Object... args) {
        log(Level.ERROR, "", args, t);
    }
}
