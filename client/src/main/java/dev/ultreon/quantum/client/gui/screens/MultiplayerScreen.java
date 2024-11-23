package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Button;
import dev.ultreon.quantum.client.gui.widget.Panel;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.Nullable;

public class MultiplayerScreen extends Screen {
    private TextEntry entry;
    private TextButton joinButton;
    private TextButton backButton;
    private Panel panel;

    public MultiplayerScreen() {
        super(Language.translate("quantum.screen.multiplayer"));
    }

    public MultiplayerScreen(@Nullable Screen back) {
        super(TextObject.translation("quantum.screen.multiplayer"), back);
    }

    @Override
    protected void init() {
        super.init();

        panel = add(Panel.create());
        panel.setPos(this.size.width / 2 - 105, this.size.height / 2 - 15);
        panel.setSize(210, 57);

        entry = add(TextEntry.of());
        entry.setPos(this.size.width / 2 - 100, this.size.height / 2 - 10);
        entry.hint().set(TextObject.translation("quantum.screen.multiplayer.server_ip"));
        entry.callback().set(this::validateServerIp);

        backButton = add(TextButton.of(UITranslations.BACK, 98));
        backButton.setPos(this.size.width / 2 - 100, this.size.height / 2 + 15);
        backButton.setType(Button.Type.DARK_EMBED);
        backButton.setCallback(this::back);

        joinButton = add(TextButton.of(TextObject.translation("quantum.screen.multiplayer.join"), 98));
        joinButton.setPos(this.size.width / 2 + 2, this.size.height / 2 + 15);
        joinButton.setType(Button.Type.DARK_EMBED);
        joinButton.setCallback(this::joinServer);
        joinButton.disable();
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.panel.setPos(width / 2 - 105, height / 2 - 15);
        this.entry.setPos(width / 2 - 100, height / 2 - 10);
        this.backButton.setPos(width / 2 - 100, height / 2 + 15);
        this.joinButton.setPos(width / 2 + 2, height / 2 + 15);
    }

    private void back(TextButton textButton) {
        this.back();
    }

    private void joinServer(TextButton caller) {
        caller.isEnabled = false;
        MessageScreen messageScreen = new MessageScreen(TextObject.translation("quantum.screen.message.joining_server"));
        this.client.showScreen(messageScreen);

        String[] split = this.entry.getValue().split(":", 2);
        if (split.length < 2) {
            return;
        }

        try {
            this.client.connectToServer(split[0], Integer.parseInt(split[1]));
        } catch (Exception e) {
            QuantumClient.LOGGER.error("Can't connect to server", e);
        }
    }

    private void validateServerIp(TextEntry caller) {
        var text = caller.getValue();
        boolean matches = text.matches("[^:]+:\\d{1,5}");
        if (!matches) {
            this.joinButton.isEnabled = false;
            return;
        }
        var split = text.split(":", 2);
        var port = Integer.parseInt(split[1]);

        if (port < 0 || port > 65535) {
            this.joinButton.isEnabled = false;
            return;
        }

        this.joinButton.isEnabled = true;
    }
}
