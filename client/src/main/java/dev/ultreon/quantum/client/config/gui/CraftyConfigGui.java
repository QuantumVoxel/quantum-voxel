package dev.ultreon.quantum.client.config.gui;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.screens.Screen;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;

public class CraftyConfigGui extends Screen {
    protected CraftyConfigGui() {
        super(TextObject.translation("quantum.screen.config"));
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(new SelectionList<CraftyConfig>(40).entries(CraftyConfig.getConfigs()).itemRenderer(this::renderItem).bounds(() -> new Bounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
    }

    private void renderItem(Renderer renderer, CraftyConfig value, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        String fileName = value.getFileName();
        renderer.textLeft(fileName, 20, y + 20, RgbColor.rgb(selected ? 0xFF0000 : 0xFFFFFF));
    }
}
