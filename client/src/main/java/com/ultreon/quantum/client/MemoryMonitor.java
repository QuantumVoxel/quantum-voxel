package com.ultreon.quantum.client;

import com.ultreon.quantum.client.gui.Notification;

/**
 * Memory monitor, used to monitor the memory usage of the game.
 * Shows a notification if the free memory available is below 10 MB.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public final class MemoryMonitor {
    private static Thread t;

    public static synchronized void start() {
        if (t != null) return;

        t = new Thread(MemoryMonitor::watch);
        t.setDaemon(true);
        t.start();
    }

    private static void watch() {
        var ref = new Object() {
            Notification memNotify = null;
        };

        while (true) {
            Runtime runtime = Runtime.getRuntime();
            long remaining = runtime.freeMemory();

            if (remaining < 10_000_000) {
                ref.memNotify = QuantumClient.invokeAndWait(() -> {
                    Notification build = Notification.builder("Low memory.", "Remaining: " + (remaining / 1024 / 1024) + " MB").sticky().build();
                    QuantumClient.get().notifications.add(build);
                    return build;
                });
            } else if (ref.memNotify != null) {
                QuantumClient.invokeAndWait(() -> {
                    ref.memNotify.close();
                    ref.memNotify = null;
                });
            }
        }
    }
}
