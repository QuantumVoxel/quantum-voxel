package dev.ultreon.quantum.client.gui.screens.config.pages;

import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.screens.config.ConfigPage;
import dev.ultreon.quantum.client.gui.screens.config.ConfigScreen;
import dev.ultreon.quantum.client.text.Language;

public class GuiConfigPage extends ConfigPage {
    private final ConfigScreen settingsScreen;

    public GuiConfigPage(ConfigScreen settingsScreen) {
        super(Language.translate("quantum.screen.options.gui.title"));
        this.settingsScreen = settingsScreen;

        addEntry(Language.translate("quantum.screen.options.gui.enableHud"), ClientConfiguration.enableHud);
        addEntry(Language.translate("quantum.screen.options.gui.enableFpsHud"), ClientConfiguration.enableFpsHud);
        addEntry(Language.translate("quantum.screen.options.gui.enableCrosshair"), ClientConfiguration.enableCrosshair);
        addEntry(Language.translate("quantum.screen.options.gui.enableVirtualKeyboard"), ClientConfiguration.enableVirtualKeyboard);
        addEntry(Language.translate("quantum.screen.options.gui.blurEnabled"), ClientConfiguration.blurEnabled);
        addEntry(Language.translate("quantum.screen.options.gui.skipSplashScreen"), ClientConfiguration.skipSplashScreen);
    }

    public ConfigScreen getSettingsScreen() {
        return settingsScreen;
    }
}
