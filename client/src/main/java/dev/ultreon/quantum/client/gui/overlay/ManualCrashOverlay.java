package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.StaticWidget;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.util.RgbColor;
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
    public void render(@NotNull Renderer renderer, float deltaTime) {
        long millis = System.currentTimeMillis();
        if (millis > this.endTime) {
            this.crash();
        }

        var width = QuantumClient.get().getScaledWidth();
        var height = QuantumClient.get().getScaledHeight();

        renderer.fill(0, 0, width, height, RgbColor.rgb(0x101010))
                .fill(0, 0, width, 2, RgbColor.rgb(0xff0000));

        renderer.textCenter("Manual Initiating Crash", 3, width / 2, height / 2 - 100)
                .textCenter("Crashing the game in " + (this.endTime - millis) / 1000 + " seconds.", width / 2, height / 2 - 50)
                .textCenter("If you didn't meant to trigger this, release any ctrl, alt or shift key.", width / 2, height / 2 - 35)
                .textCenter("If you continue, it will crash the game with all thread states in the crash log.", width / 2, height / 2 - 10);
    }

    private void crash() {
        CrashLog log = new CrashLog("Manually Initiated Crash", new Throwable(":("));
        QuantumClient.get().fillGameInfo(log);

        GamePlatform.get().debugCrash(log);

        QuantumClient.crash(log);
    }
}
