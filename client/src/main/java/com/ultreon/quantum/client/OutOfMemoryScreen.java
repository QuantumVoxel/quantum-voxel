package com.ultreon.quantum.client;

import com.ultreon.quantum.client.gui.Alignment;
import com.ultreon.quantum.client.gui.GuiBuilder;
import com.ultreon.quantum.client.gui.Position;
import com.ultreon.quantum.client.gui.screens.Screen;
import com.ultreon.quantum.client.gui.widget.Label;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Color;

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
                .textColor(Color.RED.brighter());

        // Add a label for the message
        builder.add(Label.of(TextObject.translation("quantum.screen.out_of_memory.message")))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 4 , this.getHeight() / 4))
                .scale(1);
    }
}
