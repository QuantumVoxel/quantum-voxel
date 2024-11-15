package dev.ultreon.quantum;

import dev.ultreon.quantum.crash.CrashLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@UnsafeApi
public class CrashHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Crash-Handler");
    private static final List<Consumer<CrashLog>> HANDLERS = new ArrayList<>();

    /**
     * Add a handler to the list of crash log handlers.
     *
     * @param handler the consumer function to handle the CrashLog
     */
    public static void addHandler(Consumer<CrashLog> handler) {
        // Add the handler to the list of crash log handlers
        CrashHandler.HANDLERS.add(handler);
    }

    /**
     * Handles a crash by logging it, displaying a dialog with crash details, and halting the runtime.
     *
     * @param crashLog The CrashLog object containing crash information.
     */
    public static void handleCrash(CrashLog crashLog) {
        // Save crash log to a file
        File crashLogFile = new File("crash-reports", CrashLog.getFileName());
        crashLog.writeToFile(crashLogFile);

        try {
            // Run crash handlers
            CrashHandler.processCrashHandlers(crashLog);
        } catch (Throwable t) {
            // Log error if crash handlers fail
            CrashHandler.LOGGER.error("Failed to run crash handlers:", t);
        }
    }

    /**
     * Processes the crash handlers by iterating over each handler and invoking it with the given crash log.
     * If any handler throws an exception, logs the error.
     *
     * @param crashLog The crash log to be processed by the handlers.
     */
    private static void processCrashHandlers(CrashLog crashLog) {
        for (var handler : CrashHandler.HANDLERS) {
            try {
                handler.accept(crashLog);
            } catch (Throwable e) {
                CrashHandler.LOGGER.error("Error in crash handler:", e);
            }
        }
    }
}
