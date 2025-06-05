package dev.ultreon.quantum.client.gui.screens.world;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WorldDeleteConfirmScreen extends Screen {
    @NotNull
    private final WorldStorage storage;

    public WorldDeleteConfirmScreen(@NotNull WorldStorage storage) {
        super(TextObject.translation("quantum.screen.world_delete_confirm.title").setColor(RgbColor.rgb(0xff4040)));
        this.storage = storage;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        builder.add(Label.of(this.title).withAlignment(Alignment.CENTER).textColor(RgbColor.RED).withPositioning(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .withScale(2));

        builder.add(Label.of(TextObject.translation("quantum.screen.world_delete_confirm.message1", this.storage.getDirectory().name()))
                .withAlignment(Alignment.CENTER)
                .withPositioning(() -> new Position(this.getWidth() / 2, this.getHeight() / 2)));

        builder.add(Label.of("quantum.screen.world_delete_confirm.message2")
                .withAlignment(Alignment.CENTER)
                .withPositioning(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 + 15)));

        builder.add(TextButton.of(UITranslations.PROCEED, 95)
                .withPositioning(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 50))
                .withCallback(this::deleteWorld));

        builder.add(TextButton.of(UITranslations.CANCEL, 95)
                .withPositioning(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 50))
                .withCallback(this::onBack));
    }

    private void deleteWorld(TextButton caller) {
        try {
            String name = this.storage.getDirectory().name();
            this.storage.delete();
            this.client.notifications.add(Notification.builder("World Deleted", String.format("'%s'", name)).subText("World Manager").icon(MessageIcon.DANGER).build());
        } catch (IOException e) {
            QuantumClient.crash(e);
        }
        this.back();
    }

    private void onBack(TextButton caller) {
        this.back();
    }

    public @NotNull WorldStorage getStorage() {
        return this.storage;
    }
}
