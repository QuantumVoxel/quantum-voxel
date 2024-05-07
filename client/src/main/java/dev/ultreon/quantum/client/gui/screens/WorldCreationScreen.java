package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Notification;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.icon.GenericIcon;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.gui.widget.IconButton;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.client.text.WordGenerator;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.WorldStorage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.IOException;
import java.nio.file.Paths;

public class WorldCreationScreen extends Screen {
    private static final WordGenerator WORD_GEN = new WordGenerator(new WordGenerator.Config().minSize(4).maxSize(6).named());
    @MonotonicNonNull
    private TextEntry worldNameEntry;
    @MonotonicNonNull
    private IconButton reloadButton;
    @MonotonicNonNull
    private TextButton createButton;
    private String worldName = "";

    public WorldCreationScreen() {
        super(TextObject.translation("quantum.screen.world_creation.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        var titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 60))
                .scale(2));

        titleLabel.text().set(this.getTitle());
        titleLabel.scale().set(2);

        this.worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();
        this.worldNameEntry = builder.add(TextEntry.of(this.worldName).position(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 - 20))
                .callback(this::updateWorldName)
                .hint(TextObject.translation("quantum.screen.world_creation.name")));

        this.reloadButton = builder.add(IconButton.of(GenericIcon.RELOAD).position(() -> new Position(this.getWidth() / 2 + 105, this.getHeight() / 2 - 24))
                .callback(this::regenerateName));

        this.createButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_creation.create"), 95)
                .position(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 5))
                .callback(this::createWorld));

        builder.add(TextButton.of(UITranslations.CANCEL, 95)
                .position(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 5))
                .callback(this::onBack));
    }

    private void regenerateName(IconButton iconButton) {
        this.worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();
        this.worldNameEntry.value(this.worldName);
    }

    private void onBack(TextButton caller) {
        this.back();
    }

    private void createWorld(TextButton caller) {
        WorldStorage storage = new WorldStorage(Paths.get("worlds", this.worldName));
        try {
            storage.delete();
            storage.createWorld();
            this.client.startWorld(storage);
        } catch (IOException e) {
            this.client.notifications.add(Notification.builder(TextObject.literal("Failed to create world"), TextObject.literal(e.getLocalizedMessage())).icon(MessageIcon.ERROR).build());
        }
    }

    private void updateWorldName(TextEntry caller) {
        this.worldName = caller.getValue();
    }

    public TextEntry getWorldNameEntry() {
        return this.worldNameEntry;
    }

    public TextButton getCreateButton() {
        return this.createButton;
    }

    public IconButton getReloadButton() {
        return this.reloadButton;
    }

    public String getWorldName() {
        return this.worldName;
    }
}
