package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.libs.datetime.v0.DateTime;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.icon.GenericIcon;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.client.text.WordGenerator;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WorldCreationScreen extends Screen {
    private static final WordGenerator WORD_GEN = new WordGenerator(new WordGenerator.Config().minSize(4).maxSize(6).named());
    @MonotonicNonNull
    private TextEntry worldNameEntry;
    @MonotonicNonNull
    private IconButton reloadButton;
    @MonotonicNonNull
    private TextButton createButton;
    private String worldName = "";
    private long seed;
    private final GameMode gameMode = GameMode.SURVIVAL;

    public WorldCreationScreen() {
        super(TextObject.translation("quantum.screen.world_creation.title"));
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        var titleLabel = builder.add(Label.of(title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(getWidth() / 2, getHeight() / 2 - 60))
                .scale(2));

        titleLabel.text().set(getTitle());
        titleLabel.scale().set(2);

        worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();

        builder.add(Panel.create())
                .bounds(() -> new Bounds(getWidth() / 2 - 105, getHeight() / 2 - 27, 240, 60));

        worldNameEntry = builder.add(TextEntry.of(worldName).position(() -> new Position(getWidth() / 2 - 100, getHeight() / 2 - 20))
                .callback(this::updateWorldName)
                .hint(TextObject.translation("quantum.screen.world_creation.name")));

        reloadButton = builder.add(IconButton.of(GenericIcon.RELOAD).position(() -> new Position(getWidth() / 2 + 105, getHeight() / 2 - 24))
                .setCallback(this::regenerateName));

        createButton = builder.add(TextButton.of(TextObject.translation("quantum.screen.world_creation.create"), 95)
                .position(() -> new Position(getWidth() / 2 - 100, getHeight() / 2 + 5))
                .setCallback(this::createWorld));

        builder.add(TextButton.of(UITranslations.CANCEL, 95)
                .position(() -> new Position(getWidth() / 2 + 5, getHeight() / 2 + 5))
                .setCallback(this::onBack));
    }

    private void regenerateName(IconButton iconButton) {
        worldName = WorldCreationScreen.WORD_GEN.generate() + " " + WorldCreationScreen.WORD_GEN.generate();
        worldNameEntry.value(worldName);
    }

    private void onBack(TextButton caller) {
        back();
    }

    private void createWorld(TextButton caller) {
        String folderName = WorldStorage.createFolderName();
        WorldStorage storage = new WorldStorage(QuantumClient.data("worlds").child(folderName).file());


        try {
            if (storage.exists(".")) {
                client.notifications.add(Notification.builder(TextObject.literal("Failed to create world"), TextObject.literal("World already exists")).icon(MessageIcon.ERROR).build());
                return;
            }

            storage.createDir(".");

            try {
                storage.saveInfo(new WorldSaveInfo(
                        seed,
                        World.REGION_DATA_VERSION,
                        gameMode,
                        gameMode,
                        worldName,
                        DateTime.current()
                ));
            } catch (IOException e) {
                e.printStackTrace();
                String localizedMessage = e.getLocalizedMessage();
                client.notifications.add(Notification.builder(TextObject.literal("Failed to create world"), TextObject.literal(localizedMessage.substring(0, Math.min(localizedMessage.length() - 1, 50)))).icon(MessageIcon.ERROR).build());
                return;
            }

            storage.createWorld();
            client.startWorld(storage);
        } catch (IOException e) {
            client.notifications.add(Notification.builder(TextObject.literal("Failed to create world"), TextObject.literal(e.getLocalizedMessage())).icon(MessageIcon.ERROR).build());
        }
    }

    private void updateWorldName(TextEntry caller) {
        worldName = caller.getValue();
    }

    public TextEntry getWorldNameEntry() {
        return worldNameEntry;
    }

    public TextButton getCreateButton() {
        return createButton;
    }

    public IconButton getReloadButton() {
        return reloadButton;
    }

    public String getWorldName() {
        return worldName;
    }
}
