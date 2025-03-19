package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.ColorComponent;
import dev.ultreon.quantum.client.gui.widget.components.TextComponent;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.checkerframework.common.value.qual.IntRange;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class TextButton extends Button<TextButton> {
    private final TextComponent text;
    private final ColorComponent textColor;

    protected TextButton() {
        this(200);
    }

    /**
     * @param width the width of the button
     */
    protected TextButton(@IntRange(from = 21) int width) {
        this(width, 21);
    }

    /**
     * @param width  the width of the button
     * @param height the height of the button
     */
    protected TextButton(@IntRange(from = 21) int width, @IntRange(from = 21) int height) {
        super(width, height);

        this.text = this.register(id("text"), new TextComponent(null));
        this.textColor = this.register(id("text_color"), new ColorComponent(RgbColor.WHITE));
    }

    public static TextButton of(TextObject text) {
        return TextButton.of(text, 200);
    }

    public static TextButton of(TextObject text, int width) {
        return TextButton.of(text, width, 21);
    }

    public static TextButton of(TextObject text, int width, int height) {
        TextButton button = new TextButton(width, height);
        button.text.set(text);
        return button;
    }

    public static TextButton of(String text) {
        return TextButton.of(text, 200);
    }

    public static TextButton of(String text, int width) {
        return TextButton.of(text, width, 21);
    }

    public static TextButton of(String text, int width, int height) {
        TextButton button = new TextButton(width, height);
        button.text.setRaw(text);
        return button;
    }

    @Override
    public TextButton position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public TextButton bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    @Override
    public TextButton setCallback(Callback<TextButton> callback) {
        this.callback.set(callback);
        return this;
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        this.renderButton(renderer, texture, x, y);

        TextObject textObject = this.text.get();
        if (renderer.pushScissors(this.getBounds().shrink(2))) {
            if (textObject != null) {
                renderer.textCenter(textObject, x + this.size.width / 2, y + (this.size.height / 2 - this.font.getLineHeight() / 2 + getButtonContentOffset()), this.isEnabled ? this.textColor.get() : this.textColor.get().withAlpha(0x80));
            }

            renderer.popScissors();
        }
    }

    @Override
    public String getName() {
        return "TextButton";
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    public TextComponent text() {
        return this.text;
    }

    public ColorComponent textColor() {
        return this.textColor;
    }

    public TextButton translation(String path, Object... args) {
        this.text.translate(path, args);
        return this;
    }
}
