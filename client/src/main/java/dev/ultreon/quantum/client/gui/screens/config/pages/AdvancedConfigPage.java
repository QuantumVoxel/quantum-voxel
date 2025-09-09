package dev.ultreon.quantum.client.gui.screens.config.pages;

import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.screens.config.ConfigPage;
import dev.ultreon.quantum.client.gui.screens.config.ConfigScreen;
import dev.ultreon.quantum.client.text.Language;

public class AdvancedConfigPage extends ConfigPage {
    private final ConfigScreen settingsScreen;

    public AdvancedConfigPage(ConfigScreen settingsScreen) {
        super(Language.translate("quantum.screen.options.advanced.title"));
        this.settingsScreen = settingsScreen;

        addEntry(Language.translate("quantum.screen.options.advanced.networkKeepAliveTime"), ClientConfiguration.networkKeepAliveTime);
        addEntry(Language.translate("quantum.screen.options.advanced.networkTimeout"), ClientConfiguration.networkTimeout);
        addEntry(Language.translate("quantum.screen.options.advanced.enableDebugUtils"), ClientConfiguration.enableDebugUtils);
    }

    public ConfigScreen getSettingsScreen() {
        return settingsScreen;
    }
}
