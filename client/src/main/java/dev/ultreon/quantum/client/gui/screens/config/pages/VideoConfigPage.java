package dev.ultreon.quantum.client.gui.screens.config.pages;

import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.screens.config.ConfigPage;
import dev.ultreon.quantum.client.gui.screens.config.ConfigScreen;
import dev.ultreon.quantum.client.text.Language;

public class VideoConfigPage extends ConfigPage {
    private final ConfigScreen settingsScreen;

    public VideoConfigPage(ConfigScreen settingsScreen) {
        super(Language.translate("quantum.screen.options.video.title"));
        this.settingsScreen = settingsScreen;

        addEntry(Language.translate("quantum.screen.options.video.renderDistance"), ClientConfiguration.renderDistance);
        addEntry(Language.translate("quantum.screen.options.video.entityRenderDistance"), ClientConfiguration.entityRenderDistance);
        addEntry(Language.translate("quantum.screen.options.video.fog"), ClientConfiguration.fog);
        addEntry(Language.translate("quantum.screen.options.video.enableVsync"), ClientConfiguration.enableVsync);
        addEntry(Language.translate("quantum.screen.options.video.fpsLimit"), ClientConfiguration.fpsLimit);
        addEntry(Language.translate("quantum.screen.options.video.fov"), ClientConfiguration.fov);
        addEntry(Language.translate("quantum.screen.options.video.guiScale"), ClientConfiguration.guiScale);
        addEntry(Language.translate("quantum.screen.options.video.fullscreen"), ClientConfiguration.fullscreen);
        addEntry(Language.translate("quantum.screen.options.video.showSunAndMoon"), ClientConfiguration.showSunAndMoon);
    }

    public ConfigScreen getSettingsScreen() {
        return settingsScreen;
    }
}
