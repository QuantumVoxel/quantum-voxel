package dev.ultreon.quantum.client;

import com.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Notification;
import dev.ultreon.quantum.text.TextObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

/**
 * Memory monitor, used to monitor the memory usage of the game.
 * Shows a notification if the free memory available is below 10 MB.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public final class HardwareMonitor {
    private static Thread t;

    public static synchronized void start() {
        if (t != null) return;

        t = new Thread(HardwareMonitor::watch);
        t.setDaemon(true);
        t.start();
    }

    private static void watch() {
        var ref = new Object() {
            Notification cpuNotify = null;
            Notification memNotify = null;
        };

        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        while (true) {
            if (!ClientConfig.showMemoryUsage){
                try {
                    Duration.ofSeconds(5).sleep();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            long available = memory.getAvailable();
            long total = memory.getTotal();
            double ratio = available / (double) total;

            if (available < 100_000_000) {
                if (ref.memNotify == null)
                    ref.memNotify = QuantumClient.invokeAndWait(() -> {
                        Notification build = Notification.builder("Low memory.", "Available: " + (available / 1024 / 1024) + " MB").sticky().build();
                        QuantumClient.get().notifications.add(build);
                        return build;
                    });
                else ref.memNotify.setSummary(TextObject.literal("Available: " + (available / 1024 / 1024) + " MB"));
            } else if (ref.memNotify != null) {
                QuantumClient.invokeAndWait(() -> {
                    ref.memNotify.close();
                    ref.memNotify = null;
                });
            }
        }
    }
}
