package dev.ultreon.libs.commons.v0.util;

import dev.ultreon.libs.commons.v0.UtilityClass;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils extends UtilityClass {
    public static RuntimeException utilityClass() {
        return new UnsupportedOperationException("Can't instantiate utility class.");
    }

    public static String getStackTrace() {
        return getStackTrace(new RuntimeException());
    }

    public static String getStackTrace(String message) {
        return getStackTrace(new RuntimeException(message));
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        String stackTrace = writer.toString();
        printWriter.close();
        return stackTrace;
    }
}
