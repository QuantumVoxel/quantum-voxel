package dev.ultreon.quantum.client.gui.screens.config;

import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.config.pages.*;
import dev.ultreon.quantum.client.gui.widget.tabs.Tabs;
import dev.ultreon.quantum.client.render.world.WorldRenderer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.TranslationText;
import org.jetbrains.annotations.Nullable;

public class ConfigScreen extends Screen {
    public static final TranslationText TITLE = TextObject.translation("quantum.screen.settings.title");
    private Tabs tabs;
    private int oldRenderDistance = ClientConfiguration.renderDistance.getValue();

    public ConfigScreen() {
        super(TITLE);
    }

    public ConfigScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    protected void init() {
        super.init();

        this.tabs = new Tabs(0, 30, size.width, size.height - 40, this::changeFocus);
        this.tabs.addTab(new VideoConfigPage(this));
        this.tabs.addTab(new GuiConfigPage(this));
        this.tabs.addTab(new AccessibillityConfigPage(this));
        this.tabs.addTab(new PrivacyConfigPage(this));
        this.tabs.addTab(new AdvancedConfigPage(this));
        this.add(tabs);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.tabs.resize(size.width, size.height - 40);
    }

    @Override
    public void onClosed() {
        super.onClosed();

        WorldRenderer worldRenderer = client.worldRenderer;
        if (oldRenderDistance != ClientConfiguration.renderDistance.getValue() && worldRenderer != null)
            worldRenderer.reloadChunks();

        ClientConfiguration.save();
        this.remove(this.tabs);
    }

    public Tabs getTabs() {
        return tabs;
    }
}
