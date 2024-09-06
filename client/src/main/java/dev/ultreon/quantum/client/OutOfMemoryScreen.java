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
 * <p>
 * In a game, out of memory errors can occur when the game tries to allocate more memory than
 * is available on the system. This can happen for a few reasons:
 * <ul>
 *     <li>The game tries to allocate too much memory at once, such as when loading a large
 *     world or map.</li>
 *     <li>The game tries to allocate too much memory over time, such as when generating
 *     terrain or generating a large number of objects.</li>
 *     <li>The system running the game has too little memory available, making it impossible
 *     for the game to allocate more memory.</li>
 *     <li>The game or a modification has a memory leak.</li>
 * </ul>
 * <p>
 * When an out of memory error occurs, the game should display a screen informing the player
 * that the game has run out of memory and suggesting that the player try the following:
 * Some things you can try to fix this problem:
 * <ul>
 *     <li>Try restarting the game.</li>
 *     <li>Going back to the main menu to free up some memory.</li>
 * </ul>
 * <p>
 * This screen can help the player understand what's going on and offers them clear steps
 * to fix the issue. It can prevent the game from crashing, which is a more user-friendly
 * approach.
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