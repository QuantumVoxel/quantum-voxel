package dev.ultreon.quantum.client;

import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.screens.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;

/**
 * An out of memory screen.
 * This screen is displayed when the JVM runs out of memory.
 */
public class OutOfMemoryScreen extends Screen {
    /**
     * Creates a new OutOfMemoryScreen.
     */
    protected OutOfMemoryScreen() {
        super(TextObject.translation("quantum.screen.out_of_memory"));
    }

    /**
     * Builds the screen layout.
     *
     * @param builder The GUI builder.
     */
    @Override
    public void build(GuiBuilder builder) {
        // Add a label for the screen title
        builder.add(Label.of(this.getTitle()))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 4 , this.getHeight() / 4 - 25))
                .scale(2)
                .textColor(RgbColor.RED.brighter());

        // Add a label for the message
        builder.add(Label.of(TextObject.translation("quantum.screen.out_of_memory.message")))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 4 , this.getHeight() / 4))
                .scale(1);
    }
}
