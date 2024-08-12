package dev.ultreon.quantum.client.gui.screens.settings;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.gui.CraftyConfigGui;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.screens.CreditsScreen;
import dev.ultreon.quantum.client.gui.screens.LanguageScreen;
import dev.ultreon.quantum.client.gui.screens.tabs.TabbedUI;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;

public class SettingsScreen extends TabbedUI {
    private static final TextObject TITLE = TextObject.translation("quantum.screen.settings.title");
    private final AccessibilitySettingsUI accessibilitySettingsUI = new AccessibilitySettingsUI();
    private final PersonalSettingsUI personalSettingsUI = new PersonalSettingsUI();
    private final VideoSettingsUI videoSettingsUI = new VideoSettingsUI();
    private final PrivacySettingsUI privacySettingsUI = new PrivacySettingsUI();

    public SettingsScreen() {
        super(TITLE);
    }

    @Override
    public void build(TabbedUIBuilder builder) {
        builder.add(VideoSettingsUI.TITLE, false, 2, videoSettingsUI::build).icon(QuantumClient.id("gui/settings/video"));
        builder.add(PrivacySettingsUI.TITLE, false, 3, privacySettingsUI::build).icon(QuantumClient.id("gui/settings/privacy"));
        builder.add(PersonalSettingsUI.TITLE, false, 1, personalSettingsUI::build).icon(QuantumClient.id("gui/settings/personal"));
        builder.add(AccessibilitySettingsUI.TITLE, false, 0, accessibilitySettingsUI::build).icon(QuantumClient.id("gui/settings/accessibility"));

        builder.contentBounds(() -> new Bounds(20, 50, size.width - 40, size.height - 80));
        setTabX(220);

        builder.add(TextButton.of(UITranslations.BACK, 50))
                .bounds(() -> new Bounds(20, 29, 48, 19))
                .callback(button -> back());

        builder.add(TextButton.of(TextObject.translation("quantum.screen.settings.configs"), 50))
                .bounds(() -> new Bounds(70, 29, 48, 19))
                .callback(button -> this.client.showScreen(new CraftyConfigGui(this)));

        builder.add(TextButton.of(TextObject.translation("quantum.screen.language"), 50))
                .bounds(() -> new Bounds(120, 29, 48, 19))
                .callback(button -> this.client.showScreen(new LanguageScreen(this)));

        builder.add(TextButton.of(TextObject.translation("quantum.screen.credits"), 50))
                .bounds(() -> new Bounds(170, 29, 48, 19))
                .callback(button -> this.client.showScreen(new CreditsScreen(this)));
    }
}
