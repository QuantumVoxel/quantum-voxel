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
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.NotNull;

public class RestartConfirmScreen extends Screen {
    public RestartConfirmScreen() {
        super(TextObject.translation("Quantum.screen.restart_confirm.title").setColor(RgbColor.rgb(0xff4040)));
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        builder.add(Label.of(this.title).withAlignment(Alignment.CENTER).textColor(ColorCode.RED).withPositioning(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .withScale(2));

        builder.add(Label.of(TextObject.translation("Quantum.screen.restart_confirm.message"))
                .withAlignment(Alignment.CENTER)
                .withPositioning(() -> new Position(this.getWidth() / 2, this.getHeight() / 2)));

        builder.add(TextButton.of(UITranslations.YES, 95)
                .withPositioning(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 50))
                .withCallback(this::restart));

        builder.add(TextButton.of(UITranslations.NO, 95)
                .withPositioning(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 50))
                .withCallback(this::onBack));
    }

    private void restart(TextButton caller) {
        System.exit(0);
    }

    private void onBack(TextButton caller) {
        this.back();
    }
}
