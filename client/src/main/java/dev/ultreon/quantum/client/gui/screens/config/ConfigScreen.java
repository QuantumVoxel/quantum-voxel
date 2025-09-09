package dev.ultreon.quantum.client.gui.screens.config;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.config.pages.*;
import dev.ultreon.quantum.client.gui.screens.tabs.Tab;
import dev.ultreon.quantum.client.gui.screens.tabs.Tabs;
import dev.ultreon.quantum.client.gui.widget.TabList;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.TranslationText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigScreen extends Screen {
    public static final TranslationText TITLE = TextObject.translation("quantum.screen.settings.title");
    private Tabs tabs;

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

        this.remove(this.tabs);
    }

    public void showPage(TabList.Page page) {
    }

    public Tabs getTabs() {
        return tabs;
    }

    public static class GeneralSettingsPage extends Tab {
        private final ConfigScreen settingsScreen;

        public GeneralSettingsPage(ConfigScreen settingsScreen) {
            super(Language.translate("quantum.screen.settings.general"));
            this.settingsScreen = settingsScreen;
        }

        @Override
        public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
            renderer.drawPlatform(pos.x - 2, pos.y, this.size.width + 4, this.size.height);

            super.renderWidget(renderer, deltaTime);
        }

        public ConfigScreen getSettingsScreen() {
            return settingsScreen;
        }
    }
}
