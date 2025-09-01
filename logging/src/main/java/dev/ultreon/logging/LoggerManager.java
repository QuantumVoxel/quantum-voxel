package dev.ultreon.logging;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class LoggerManager {
    private static final LoggerManager INSTANCE = new LoggerManager();
    private final Map<String, Logger> loggers = new HashMap<>();
    public final PrintStream out;
    public final PrintStream debugOut;
    private final PrintStream oldOut;
    private final PrintStream oldErr;
    private LogLevel level = LogLevel.INFO;

    private LoggerManager() {
        API.PROPERTY.get().addShutdownHook(this::close);

        if (System.getProperty("ultreon.log.level") != null) {
            try {
                level = LogLevel.valueOf(System.getProperty("ultreon.log.level").toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid log level: " + System.getProperty("ultreon.log.level"));
            }
        }

        FileHandle logsDir = Gdx.files.local("logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        FileHandle latestLog = Gdx.files.local("logs/latest.log");
        FileHandle debugLog = Gdx.files.local("logs/debug.log");

        compressIfExists(latestLog);
        compressIfExists(debugLog);

        debugOut = new MultiPrintStream(true, System.out, new PrintStream(new ANSIEscapingOutputStream(debugLog.write(false)), true));
        out = new MultiPrintStream(true, debugOut, new PrintStream(new ANSIEscapingOutputStream(latestLog.write(false)), true));

        oldOut = System.out;
        oldErr = System.err;
    }

    private void compressIfExists(FileHandle path) {
        if (path.exists()) {
            path.delete();
        }
    }

    private void close() {
        out.close();

        API.PROPERTY.get().setOut(oldOut);
        API.PROPERTY.get().setErr(oldErr);

        loggers.clear();
        INSTANCE.loggers.clear();
    }

    public static Logger getLogger(String name) {
        return INSTANCE.loggers.computeIfAbsent(name, INSTANCE::createLogger);
    }

    public Logger createLogger(String name) {
        return new Logger(this, name);
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }
}
