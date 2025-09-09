package dev.ultreon.quantum.client.gui.screens.config.pages;

import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.screens.config.ConfigPage;
import dev.ultreon.quantum.client.gui.screens.config.ConfigScreen;
import dev.ultreon.quantum.client.text.Language;

public class PrivacyConfigPage extends ConfigPage {
    private final ConfigScreen settingsScreen;

    public PrivacyConfigPage(ConfigScreen settingsScreen) {
        super(Language.translate("quantum.screen.options.privacy.title"));
        this.settingsScreen = settingsScreen;

        addEntry(Language.translate("quantum.screen.options.privacy.hideActivity"), ClientConfiguration.hideActivity);
        addEntry(Language.translate("quantum.screen.options.privacy.hideSkin"), ClientConfiguration.hideSkin);
        addEntry(Language.translate("quantum.screen.options.privacy.hideUsername"), ClientConfiguration.hideUsername);
        addEntry(Language.translate("quantum.screen.options.privacy.hideServerFromActivity"), ClientConfiguration.hideServerFromActivity);
        addEntry(Language.translate("quantum.screen.options.privacy.hideActivity"), ClientConfiguration.hideActivity);
    }

    public ConfigScreen getSettingsScreen() {
        return settingsScreen;
    }
}
