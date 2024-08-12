package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.text.TextObject;

public class ShutdownScreen extends Screen {
    private Label message;

    protected ShutdownScreen() {
        super(TextObject.translation("quantum.screen.shutdown"));
    }

    @Override
    public void build(GuiBuilder builder) {
        this.message = builder.add(Label.of(TextObject.translation("Quantum.screen.shutdown.message")))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 2 - 40))
                .scale(2);
    }

    public void setMessage(String message) {
        this.message.text().setRaw(message);
    }
}
