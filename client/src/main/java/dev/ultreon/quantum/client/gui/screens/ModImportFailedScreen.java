package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

public class ModImportFailedScreen extends Screen {
    public ModImportFailedScreen() {
        super(TextObject.translation("Quantum.screen.import_failed.title"));
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        builder.add(Label.of(this.title).alignment(Alignment.CENTER).textColor(ColorCode.RED).withPositioning(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .scale(2));

        builder.add(Label.of(TextObject.translation("Quantum.screen.import_failed.message"))
                .alignment(Alignment.CENTER)
                .withPositioning(() -> new Position(this.getWidth() / 2, this.getHeight() / 2)));

        builder.add(TextButton.of(UITranslations.OK, 100)
                .withPositioning(() -> new Position(this.getWidth() / 2 - 50, this.getHeight() / 2 + 50))
                .withCallback(this::onBack));
    }

    private void onBack(TextButton caller) {
        this.back();
    }
}
