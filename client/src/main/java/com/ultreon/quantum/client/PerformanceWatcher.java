package com.ultreon.quantum.client;

import com.ultreon.quantum.client.gui.Notification;

public class PerformanceWatcher {
    public PerformanceWatcher() {
        Thread t = new Thread(this::watch);
    }

    private void watch() {
        Notification memNotify = null;

        while (true) {
            Runtime runtime = Runtime.getRuntime();
            long remaining = runtime.freeMemory();

            if (remaining < 10_000_000) {
                memNotify = QuantumClient.invokeAndWait(() -> {
                    Notification build = Notification.builder("Low memory.", "Remaining: " + (remaining / 1024 / 1024) + " MB").sticky().build();
                    QuantumClient.get().notifications.add(build);
                    return build;
                });
            } else if (memNotify != null) {
                memNotify.close();
                memNotify = null;
            }
        }
    }
}
