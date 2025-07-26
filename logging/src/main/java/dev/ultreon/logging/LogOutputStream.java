package dev.ultreon.logging;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LogOutputStream extends OutputStream {
    private final Logger logger;
    private final StringBuffer buffer = new StringBuffer();
    private final PrintStream out;

    public LogOutputStream(LoggerManager loggerManager, PrintStream out, String name) {
        super();
        this.out = out;

        this.logger = loggerManager.createLogger(name);
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (this) {
            if (b == '\n' || b == '\r') {
                flush();
            } else {
                buffer.append((char) b);
            }
        }
    }

    @Override
    public void flush() {
        if (buffer.length() == 0) {
            return;
        }
        try {
            logger.log(System.currentTimeMillis(), LogLevel.INFO, LogCategory.DEFAULT, buffer.toString(), null, false, false);
        } catch (Throwable e) {
            out.println("Failed to log message: " + e.getMessage());
        }
        buffer.setLength(0);
    }
}
