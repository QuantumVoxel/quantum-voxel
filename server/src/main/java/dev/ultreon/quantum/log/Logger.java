package dev.ultreon.quantum.log;

import java.io.File;

public interface Logger {
    void debug(String s);

    void debug(String s, Object o);

    void debug(String s, Throwable t);

    void debug(String s, Object p, Object o);

    void debug(String s, Object p, Throwable t);

    void info(String s);

    void info(String s, Object o);

    void info(String s, Object p, Object o);

    void info(String s, Object p, Throwable t);

    void info(String s, Throwable t);

    void warn(String s);

    void warn(String s, Object o);

    void warn(String s, Object p, Object o);

    void warn(String s, Object p, Throwable t);

    void warn(String s, Throwable t);

    void error(String s);

    void error(String s, Object o);

    void error(String s, Throwable t);

    void error(String s, Object p, Object o);

    void error(String s, Object p, Throwable t);
}
