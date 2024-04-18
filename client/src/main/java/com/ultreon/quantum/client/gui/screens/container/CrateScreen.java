package com.ultreon.quantum.client.gui.screens.container;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.menu.CrateMenu;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Identifier;

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
}
