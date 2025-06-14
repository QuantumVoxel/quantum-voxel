package dev.ultreon.quantum.client.config.gui;

import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CraftyConfigGui extends Screen {
    private final Collection<? extends CraftyConfig> configs;

    public CraftyConfigGui(Screen screen) {
        this(screen, CraftyConfig.getConfigs());
    }

    public CraftyConfigGui(Screen screen, Mod mod) {
        this(screen, CraftyConfig.getByMod(mod));
    }

    public CraftyConfigGui(Screen screen, Collection<? extends CraftyConfig> configs) {
        super(TextObject.translation("quantum.screen.config"), screen);
        this.configs = configs;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        builder.add(new SelectionList<CraftyConfig>(40)
                .addEntries(configs)
                .withItemRenderer(this::renderItem)
                .withSelectable(true)
                .withDrawBackground(true)
                .withCallback((config) -> {
                    ConfigGui configGui = new ConfigGui(this, config);
                    this.client.showScreen(configGui);
                })
                .withBounding(() -> new Bounds(10, 10, this.size.width - 20, this.size.height - 20)));
    }

    private void renderItem(Renderer renderer, CraftyConfig value, int y, boolean selected, float deltaTime) {
        String fileName = value.getFileName();
        renderer.textLeft(fileName, 20, y + 20, RgbColor.rgb(selected ? 0xFF0000 : 0xFFFFFF));
    }
}
