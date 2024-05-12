package dev.ultreon.quantum.desktop.DesktopLogger;

import dev.ultreon.quantum.log.Logger;

public class Slf4jLogger implements Logger {
    private final org.slf4j.Logger slf4jLogger;

    public Slf4jLogger(org.slf4j.Logger slf4jLogger) {
        this.slf4jLogger = slf4jLogger;
    }

    @Override
    public void debug(String s) {
        slf4jLogger.debug(s);
    }

    @Override
    public void debug(String s, Object o) {
        slf4jLogger.debug(s, o);
    }

    @Override
    public void debug(String s, Throwable t) {
        slf4jLogger.debug(s, t);
    }

    @Override
    public void debug(String s, Object p, Object o) {
        slf4jLogger.debug(s, p, o);
    }

    @Override
    public void debug(String s, Object p, Throwable t) {
        slf4jLogger.debug(s, p, t);
    }

    @Override
    public void info(String s) {
        slf4jLogger.info(s);
    }

    @Override
    public void info(String s, Object o) {
        slf4jLogger.info(s, o);
    }

    @Override
    public void info(String s, Object p, Object o) {
        slf4jLogger.info(s, p, o);
    }

    @Override
    public void info(String s, Object p, Throwable t) {
        slf4jLogger.info(s, p, t);
    }

    @Override
    public void info(String s, Throwable t) {
        slf4jLogger.info(s, t);
    }

    @Override
    public void warn(String s) {
        slf4jLogger.warn(s);
    }

    @Override
    public void warn(String s, Object o) {
        slf4jLogger.warn(s, o);
    }

    @Override
    public void warn(String s, Object p, Object o) {
        slf4jLogger.warn(s, p, o);
    }

    @Override
    public void warn(String s, Object p, Throwable t) {
        slf4jLogger.warn(s, p, t);
    }

    @Override
    public void warn(String s, Throwable t) {
        slf4jLogger.warn(s, t);
    }

    @Override
    public void error(String s) {
        slf4jLogger.error(s);
    }

    @Override
    public void error(String s, Object o) {
        slf4jLogger.error(s, o);
    }

    @Override
    public void error(String s, Throwable t) {
        slf4jLogger.error(s, t);
    }

    @Override
    public void error(String s, Object p, Object o) {
        slf4jLogger.error(s, p, o);
    }

    @Override
    public void error(String s, Object p, Throwable t) {
        slf4jLogger.error(s, p, t);
    }
}
