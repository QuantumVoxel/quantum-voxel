package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Notification;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class WorldDeleteConfirmScreen extends Screen {
    @NotNull
    private final WorldStorage storage;

    public WorldDeleteConfirmScreen(@NotNull WorldStorage storage) {
        super(TextObject.translation("quantum.screen.world_delete_confirm.title").setColor(RgbColor.rgb(0xff4040)));
        this.storage = storage;
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title).alignment(Alignment.CENTER).textColor(RgbColor.RED).position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .scale(2));

        builder.add(Label.of(TextObject.translation("quantum.screen.world_delete_confirm.message1", this.storage.getDirectory().getFileName().toString()))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2)));

        builder.add(Label.of("quantum.screen.world_delete_confirm.message2")
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 + 15)));

        builder.add(TextButton.of(UITranslations.PROCEED, 95)
                .position(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 50))
                .callback(this::deleteWorld));

        builder.add(TextButton.of(UITranslations.CANCEL, 95)
                .position(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 50))
                .callback(this::onBack));
    }

    private void deleteWorld(TextButton caller) {
        try {
            Path name = this.storage.getDirectory().getFileName();
            this.storage.delete();
            this.client.notifications.add(Notification.builder("World Deleted", "'%s'".formatted(name)).subText("World Manager").icon(MessageIcon.DANGER).build());
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
