package dev.ultreon.quantum.client;

import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a screen displayed when the Java Virtual Machine (JVM) encounters an
 * {@link OutOfMemoryError}.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class OutOfMemoryScreen extends Screen {
    /**
     * Creates a new OutOfMemoryScreen.
     */
    @ApiStatus.Internal
    OutOfMemoryScreen() {
        super(TextObject.translation("quantum.screen.out_of_memory"));
    }

    /**
     * Builds the screen layout.
     * This method is called when the screen is first displayed.
     *
     * @param builder The GUI builder used to create the screen layout.
     */
    @Override
    public void build(@NotNull GuiBuilder builder) {
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