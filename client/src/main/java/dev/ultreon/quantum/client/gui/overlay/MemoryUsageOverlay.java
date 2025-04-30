package dev.ultreon.quantum.client.gui.overlay;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;

public class MemoryUsageOverlay extends Overlay {

    public static final Color LINE_COLOR = new Color(0x0080ff);

    @Override
    public void render(Renderer renderer, float deltaTime) {
        if (!ClientConfiguration.showMemoryUsage.getValue()) return;

        int scrWidth = QuantumClient.get().getScaledWidth();
        int scrHeight = QuantumClient.get().getScaledHeight();

        int width = 180;
        int height = 40;
        int x = scrWidth - width - 10;
        int y = scrHeight - height - 10;

        renderer.fill(x + 1, y, width + 4, height + 6, RgbColor.rgb(0x202020));
        renderer.fill(x, y + 1, width + 6, height + 4, RgbColor.rgb(0x202020));
        renderer.box(x + 1, y + 1, width + 4, height + 4, RgbColor.rgb(0x303030));
        GamePlatform runtime = GamePlatform.get();

        long maxMemory = runtime.maxMemory(); // Maximum amount of memory the JVM will attempt to use
        long allocatedMemory = runtime.totalMemory(); // Total amount of memory in use by the JVM
        long freeMemory = runtime.freeMemory(); // Amount of free memory within the allocated memory

//        System.out.println("Max memory: " + maxMemory);
//        System.out.println("Allocated memory: " + allocatedMemory);
//        System.out.println("Free memory: " + freeMemory);
//        System.out.println("Percentage used: " + ((double) (allocatedMemory - freeMemory) / maxMemory) * 100);

        renderer.textLeft(TextObject.translation("quantum.hud.memory_usage").setColor(ColorCode.WHITE), x + 5, y + 5, RgbColor.WHITE);
        renderer.textLeft(TextObject.translation("quantum.hud.memory_usage.used", ((allocatedMemory) / 1000000), TextObject.translation("quantum.misc.megabytes"), " / ", maxMemory / 1000000, TextObject.translation("quantum.misc.megabytes")).setColor(ColorCode.GRAY), x + 5, y + 15, RgbColor.WHITE);
        renderer.textLeft(TextObject.translation("quantum.hud.memory_usage.jvm_free", (freeMemory / 1000000), TextObject.translation("quantum.misc.megabytes")).setColor(ColorCode.GRAY), x + 5, y + 25, RgbColor.WHITE);

        renderer.line(x + 1, y + height + 4, x + ((float) (allocatedMemory) / maxMemory * width + 4) - 1, y + height + 4, LINE_COLOR);
    }
}
