package dev.ultreon.quantum.client.gui.screens.world;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.WorldSaveInfo;
import dev.ultreon.quantum.world.WorldStorage;

import java.io.IOException;

public class WorldEditScreen extends Screen {
    private final WorldStorage world;
    private String nameToSet;
    private Label titleLabel;
    private Platform platform;
    private TextEntry worldNameEntry;
    private TextButton saveButton;
    private TextButton cancelButton;

    public WorldEditScreen(WorldStorage world) {
        super(TextObject.translation("quantum.screen.worlds.edit.title"));
        this.world = world;
    }

    public WorldStorage getWorld() {
        return world;
    }

    @Override
    public void init() {
        titleLabel = add(Label.of(title)
                .withAlignment(Alignment.CENTER)
                .withScale(2));

        titleLabel.text().set(getTitle());
        titleLabel.scale().set(2);


        platform = add(Platform.create());

        worldNameEntry = add(TextEntry.create()
                .withValue(world.getName())
                .withHint(TextObject.translation("quantum.screen.world_creation.name")));

        saveButton = add(TextButton.of(TextObject.translation("quantum.ui.save"), 97)
                .withCallback(this::save));

        cancelButton = add(TextButton.of(UITranslations.CANCEL, 97)
                .withCallback(this::back));
    }

    private void back(TextButton textButton) {
        back();
    }

    @Override
    public void resized(int width, int height) {
        titleLabel.setPos(getWidth() / 2, getHeight() / 2 - 60);
        platform.setBounds(getWidth() / 2 - 105, getHeight() / 2 - 27, 210, 60);

        worldNameEntry.setPos(getWidth() / 2 - 100, getHeight() / 2 - 20);
        saveButton.setPos(getWidth() / 2 - 100, getHeight() / 2 + 5);
        cancelButton.setPos(getWidth() / 2 + 2, getHeight() / 2 + 5);
    }

    private void cancel(TextButton textButton) {
        back();
    }

    private void save(TextButton textButton) {
        WorldSaveInfo worldSaveInfo = this.world.loadInfo();
        worldSaveInfo.setName(this.worldNameEntry.getValue());
        try {
            worldSaveInfo.save(this.world);
        } catch (IOException e) {
            showDialog(new DialogBuilder(this)
                    .title(TextObject.translation("quantum.dialog.error.title"))
                    .message(TextObject.translation("quantum.dialog.error.message", e.getLocalizedMessage()))
                    .button(TextObject.translation("quantum.dialog.ok"), dialog -> {
                        QuantumClient.LOGGER.error(e.getMessage(), e);
                        closeDialog(dialog);
                    }));
        }
        back();
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    private void updateName(TextEntry textEntry) {
        this.nameToSet = textEntry.getValue();
    }
}
