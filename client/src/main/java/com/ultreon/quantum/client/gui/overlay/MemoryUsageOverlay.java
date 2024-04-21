package com.ultreon.quantum.client.gui.overlay;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.config.Config;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.util.Color;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

public class MemoryUsageOverlay extends Overlay {
    private final SystemInfo systemInfo = new SystemInfo();

    @Override
    public void render(Renderer renderer, float deltaTime) {
        if (!Config.showMemoryUsage) return;

        int scrWidth = QuantumClient.get().getScaledWidth();
        int scrHeight = QuantumClient.get().getScaledHeight();

        int width = 180;
        int height = 40;
        int x = scrWidth - width - 10;
        int y = scrHeight - height - 10;

        GlobalMemory memory = systemInfo.getHardware().getMemory();

        renderer.fill(x + 1, y, width + 4, height + 6, Color.rgb(0x202020));
        renderer.fill(x, y + 1, width + 6, height + 4, Color.rgb(0x202020));
        renderer.box(x + 1, y + 1, width + 4, height + 4, Color.rgb(0x303030));

        renderer.textLeft("<bold>Memory Usage", x + 5, y + 5, Color.WHITE);
        renderer.textLeft("Used: " + ((memory.getTotal() - memory.getAvailable()) / 1000000) + " MB / " + (memory.getTotal() / 1000000) + " MB", x + 5, y + 15, Color.WHITE);
        renderer.textLeft("VM Free: " + (Runtime.getRuntime().freeMemory() / 1000000) + " MB", x + 5, y + 25, Color.WHITE);

        renderer.line(x + 1, y + height + 4, x + ((float) (memory.getTotal() - memory.getAvailable()) / memory.getTotal() * width + 4) - 1, y + height + 4, Color.AZURE);
    }
}
