package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.TranslationText;
import org.jetbrains.annotations.NotNull;

public class MessageScreen extends Screen {
    private TextObject deferredMessage = TextObject.empty();
    private Label messageLabel;

    public MessageScreen(TextObject title) {
        super(title);
    }

    public MessageScreen(TranslationText title, String message) {
        super(title);

        this.deferredMessage = TextObject.nullToEmpty(message);
    }

    public MessageScreen(TranslationText title, TranslationText message) {
        super(title);

        this.deferredMessage = message;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 - 40))
                .scale(2));

        this.messageLabel = builder.add(Label.of(deferredMessage)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3)));
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderSolidBackground(renderer);
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    public MessageScreen message(TextObject message) {
        this.messageLabel.text().set(message);
        return this;
    }

    public MessageScreen message(String message) {
        this.messageLabel.text().setRaw(message);
        return this;
    }
}
