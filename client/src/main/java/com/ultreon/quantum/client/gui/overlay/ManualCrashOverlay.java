package com.ultreon.quantum.client.gui.overlay;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.client.gui.widget.StaticWidget;
import com.ultreon.quantum.crash.CrashCategory;
import com.ultreon.quantum.crash.CrashLog;
import com.ultreon.quantum.util.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ManualCrashOverlay implements StaticWidget {
    private long endTime;

    /**
     * Creates a new manual crash overlay.
     *
     * @param client the Quantum Voxel client
     */
    public ManualCrashOverlay(QuantumClient client) {
        super();
    }

    /**
     * Resets the manual crash timer.
     */
    public void reset() {
        this.endTime = System.currentTimeMillis() + 10000; // Crash the game in 10 seconds.
    }

    @Override
    public void render(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        long millis = System.currentTimeMillis();
        if (millis > this.endTime) {
            this.crash();
        }

        var width = QuantumClient.get().getScaledWidth();
        var height = QuantumClient.get().getScaledHeight();

        renderer.fill(0, 0, width, height, Color.rgb(0x101010))
                .fill(0, 0, width, 2, Color.rgb(0xff0000));

        renderer.textCenter("Manual Initiating Crash", 3, width / 2, height / 2 - 100)
                .textCenter("Crashing the game in " + (this.endTime - millis) / 1000 + " seconds.", width / 2, height / 2 - 50)
                .textCenter("If you didn't meant to trigger this, release any ctrl, alt or shift key.", width / 2, height / 2 - 35)
                .textCenter("If you continue, it will crash the game with all thread states in the crash log.", width / 2, height / 2 - 10);
    }

    private void crash() {
        CrashLog log = new CrashLog("Manually Initiated Crash", new Throwable(":("));
        QuantumClient.get().fillGameInfo(log);

        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            StackTraceElement[] stackTrace = entry.getValue();
            String name = entry.getKey().getName();
            long id = entry.getKey().getId();

            Throwable throwable = new Throwable();
            throwable.setStackTrace(stackTrace);

            CrashCategory threadCategory = new CrashCategory("Thread #" + id + ": " + name, throwable);
            log.addCategory(threadCategory);
        }

        QuantumClient.crash(log);
    }
}
