package dev.ultreon.quantum.client.gui.screens.config.pages;

import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.screens.config.ConfigPage;
import dev.ultreon.quantum.client.gui.screens.config.ConfigScreen;
import dev.ultreon.quantum.client.text.Language;

public class AccessibillityConfigPage extends ConfigPage {
    private final ConfigScreen settingsScreen;

    public AccessibillityConfigPage(ConfigScreen settingsScreen) {
        super(Language.translate("quantum.screen.options.accessibility.title"));
        this.settingsScreen = settingsScreen;

        addEntry(Language.translate("quantum.screen.options.accessibility.closePrompt"), ClientConfiguration.closePrompt);
        addEntry(Language.translate("quantum.screen.options.accessibility.showFirstPersonPlayer"), ClientConfiguration.firstPersonPlayerModel);
        addEntry(Language.translate("quantum.screen.options.accessibility.showHotbarWhenThirdPerson"), ClientConfiguration.thirdpersonHotbar);
        addEntry(Language.translate("quantum.screen.options.accessibility.cameraSensitivity"), ClientConfiguration.cameraSensitivity);
        addEntry(Language.translate("quantum.screen.options.accessibility.controllerDeadZone"), ClientConfiguration.controllerDeadZone);
    }

    public ConfigScreen getSettingsScreen() {
        return settingsScreen;
    }
}
