package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.AlignmentComponent;
import dev.ultreon.quantum.client.gui.widget.components.ColorComponent;
import dev.ultreon.quantum.client.gui.widget.components.ScaleComponent;
import dev.ultreon.quantum.client.gui.widget.components.TextComponent;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Color;
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

/**
 * A simple label widget for displaying text.
 *
 * <p>Label widgets are used to display text on the screen.
 * They are typically used to display
 * text not meant to be edited by the user.
 * These widgets are essentially just text
 * components that are designed to be displayed statically.
 *
 * <p>Labels support text alignment and coloring.
 * The text alignment of a label determines how
 * the text is positioned within the widget's bounds.
 * The text color of a label determines the
 * color of the text displayed on the screen.
 *
 * <p>Labels are static and do not support user interaction.
 * They are typically used to display text meant to be
 * informational or for decorative purposes.
 *
 * @author XyperCode
 * @see TextObject
 */
@ApiStatus.NonExtendable
public class Label extends Widget {
    private final AlignmentComponent alignment;
    private final ColorComponent textColor;
    private final TextComponent text;
    private final ScaleComponent scale;

    /**
     * Creates a new label with the left alignment and white text color.
     */
    public Label() {
        this(Alignment.LEFT, RgbColor.WHITE);
    }

    /**
     * Creates a new label with the given text color.
     *
     * @param textColor the color of the text
     */
    public Label(RgbColor textColor) {
        this(Alignment.LEFT, textColor);
    }

    /**
     * Creates a new label with the given alignment.
     *
     * @param alignment the alignment of the text
     */
    public Label(Alignment alignment) {
        this(alignment, RgbColor.WHITE);
    }

    /**
     * Creates a new label with the given alignment and text color.
     *
     * @param alignment the alignment of the text
     * @param textColor the color of the text
     */
    public Label(Alignment alignment, RgbColor textColor) {
        super(0, 0);
        this.alignment = this.register(id("alignment"), new AlignmentComponent(alignment));
        this.textColor = this.register(id("text_color"), new ColorComponent(textColor));
        this.text = this.register(id("text"), new TextComponent(null));
        this.scale = this.register(id("scale"), new ScaleComponent(1));
    }

    /**
     * Creates a new label with the given text.
     *
     * @param text the text to display on the label
     * @return the created label
     */
    public static Label of(TextObject text) {
        Label label = new Label();
        label.text.set(text);
        return label;
    }

    /**
     * Creates a new label with the given raw text.
     *
     * @param text the raw text to display on the label
     * @return the created label
     */
    public static Label of(String text) {
        Label label = new Label();
        label.text.setRaw(text);
        return label;
    }

    /**
     * Creates a new empty label.
     *
     * @return the created label
     */
    public static Label of() {
        Label label = new Label();
        label.text.set(TextObject.empty());
        return label;
    }

    /**
     * Sets the position of the label using a supplier.
     * When the widget is revalidated, the position of the label will
     * be updated using the result of the supplier.
     *
     * @param position the supplier which provides the position of the label
     * @return this label
     */
    @Override
    public Label position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    /**
     * Sets the bounds of the label using a supplier.
     * When the widget is revalidated, the bounds of the label will
     * be updated using the result of the supplier.
     *
     * @param position the supplier which provides the bounds of the label
     * @return this label
     */
    @Override
    public Label bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    /**
     * Sets the alignment of the label.
     *
     * @param alignment the alignment of the label
     * @return this label
     */
    public Label alignment(Alignment alignment) {
        this.alignment.set(alignment);
        return this;
    }

    /**
     * Sets the text color of the label.
     *
     * @param textColor the text color the label
     * @return this label
     */
    public Label textColor(Color textColor) {
        this.textColor.set(textColor);
        return this;
    }

    /**
     * Sets the scale of the text of the label.
     *
     * @param scale the scale of the text of the label
     * @return this label
     */
    public Label scale(int scale) {
        this.scale.set(scale);
        return this;
    }

    /**
     * Renders the background of the label.
     *
     * @param renderer the renderer to use for rendering
     * @param deltaTime the time that has passed since the last frame
     */
    @Override
    public void renderBackground(Renderer renderer, float deltaTime) {
        this.size.idt();
        int scale = this.scale.get();
        var text = this.text.get();
        var textColor = this.textColor.get();

        if (text == null) return;

        switch (this.alignment.get()) {
            case LEFT:
                renderer.textLeft(text, scale, this.pos.x, this.pos.y, textColor);
                break;
            case CENTER:
                renderer.textCenter(text, scale, this.pos.x, this.pos.y, textColor);
                break;
            case RIGHT:
                renderer.textRight(text, scale, this.pos.x, this.pos.y, textColor);
                break;
        }
    }

    /**
     * Returns the name of the widget.
     *
     * @return the name of the widget
     */
    @Override
    public String getName() {
        return "Label";
    }

    /**
     * Returns the scale component of the label.
     *
     * @return the scale component of the label
     */
    public ScaleComponent scale() {
        return this.scale;
    }

    /**
     * Returns the text component of the label.
     *
     * @return the text component of the label
     */
    public TextComponent text() {
        return this.text;
    }

    /**
     * Returns the alignment component of the label.
     *
     * @return the alignment component of the label
     */
    public AlignmentComponent alignment() {
        return this.alignment;
    }
}
