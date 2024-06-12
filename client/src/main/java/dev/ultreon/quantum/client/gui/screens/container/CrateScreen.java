package dev.ultreon.quantum.client.gui.screens.container;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.CrateMenu;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Identifier;

import java.util.List;

public class CrateScreen extends ContainerScreen {
    private static final int CONTAINER_SIZE = 40;
    private static final Identifier BACKGROUND = QuantumClient.id("textures/gui/container/crate.png");
    private final CrateMenu menu;

    public CrateScreen(CrateMenu menu, TextObject title) {
        super(menu, title, CrateScreen.CONTAINER_SIZE);
        this.menu = menu;
    }

    @Override
    public int backgroundWidth() {
        return 181;
    }

    @Override
    public int backgroundHeight() {
        return 186;
    }

    @Override
    public Identifier getBackground() {
        return CrateScreen.BACKGROUND;
    }

    @Override
    public void setup(List<ItemStack> items) {
        this.menu.setupClient(items);
    }

    public CrateMenu getMenu() {
        return this.menu;
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.textLeft(TextObject.translation("quantum.container.inventory.title"), left() + 10, top() + 74);
    }
}
