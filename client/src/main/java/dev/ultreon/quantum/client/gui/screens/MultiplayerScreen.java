package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Button;
import dev.ultreon.quantum.client.gui.widget.Panel;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiplayerScreen extends Screen {
    private TextEntry entry;
    private TextButton joinButton;

    public MultiplayerScreen() {
        super(Language.translate("quantum.screen.multiplayer"));
    }

    public MultiplayerScreen(@Nullable Screen back) {
        super(TextObject.translation("quantum.screen.multiplayer"), back);
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        builder.add(Panel.create().bounds(() -> new Bounds(this.size.width / 2 - 105, this.size.height / 2 - 15, 210, 57)));

        this.entry = builder.add(TextEntry.of().position(() -> new Position(this.size.width / 2 - 100, this.size.height / 2 - 10)))
                .callback(this::validateServerIp)
                .hint(TextObject.translation("quantum.screen.multiplayer.server_ip"));


        this.joinButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.multiplayer.join"), 98)
                .position(() -> new Position(this.size.width / 2 + 2, this.size.height / 2 + 15))
                .getType(Button.Type.DARK_EMBED)
                .setCallback(this::joinServer));
        this.joinButton.disable();
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
