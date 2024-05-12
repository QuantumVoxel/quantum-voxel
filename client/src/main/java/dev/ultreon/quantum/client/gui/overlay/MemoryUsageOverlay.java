package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.util.RgbColor;

public class MemoryUsageOverlay extends Overlay {
    @Override
    public void render(Renderer renderer, float deltaTime) {
        if (!ClientConfig.showMemoryUsage) return;

        int scrWidth = QuantumClient.get().getScaledWidth();
        int scrHeight = QuantumClient.get().getScaledHeight();

        int width = 180;
        int height = 40;
        int x = scrWidth - width - 10;
        int y = scrHeight - height - 10;

        renderer.fill(x + 1, y, width + 4, height + 6, RgbColor.rgb(0x202020));
        renderer.fill(x, y + 1, width + 6, height + 4, RgbColor.rgb(0x202020));
        renderer.box(x + 1, y + 1, width + 4, height + 4, RgbColor.rgb(0x303030));

        renderer.textLeft("<bold>Memory Usage", x + 5, y + 5, RgbColor.WHITE);
//        renderer.textLeft("Used: " + ((memory.getTotal() - memory.getAvailable()) / 1000000) + " MB / " + (memory.getTotal() / 1000000) + " MB", x + 5, y + 15, RgbColor.WHITE);
        renderer.textLeft("VM Free: " + (Runtime.getRuntime().freeMemory() / 1000000) + " MB", x + 5, y + 25, RgbColor.WHITE);

//        renderer.line(x + 1, y + height + 4, x + ((float) (memory.getTotal() - memory.getAvailable()) / memory.getTotal() * width + 4) - 1, y + height + 4, RgbColor.AZURE);
    }
}
