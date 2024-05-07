package dev.ultreon.quantum.client;

import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

/**
 * Development preview screen. Shows up when the game is still in development.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class DevScreen extends Screen {
    /**
     * Constructor for DevScreen class.
     * Initializes with a translation text for the screen title.
     */
    protected DevScreen() {
        super(TextObject.translation("quantum.screen.dev"));
    }

    /**
     * Builds the GUI layout for the DevScreen.
     * Adds a label and a text button to the builder.
     * Sets their positions and alignments.
     * Closes the screen when the button is clicked.
     *
     * @param builder The GuiBuilder object to build the GUI layout.
     */
    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(TextObject.translation("quantum.screen.dev.message"))
                .alignment(Alignment.LEFT)
                .position(() -> new Position(40, 40)));

        builder.add(TextButton.of(TextObject.translation("quantum.screen.dev.close"))
                .bounds(() -> new Bounds(client.getScaledWidth() / 2 - 50, client.getScaledHeight() - 40, 100, 20))
                .callback(caller -> this.close()));
    }

    /**
     * Renders the widget for the DevScreen.
     * Calls the superclass method to render the background.
     *
     * @param renderer  The Renderer object to render the widget.
     * @param mouseX    The x-coordinate of the mouse.
     * @param mouseY    The y-coordinate of the mouse.
     * @param deltaTime The time passed since the last frame.
     */
    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        renderBackground(renderer);

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }
}
