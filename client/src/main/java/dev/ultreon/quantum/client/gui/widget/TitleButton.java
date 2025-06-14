package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.ColorComponent;
import dev.ultreon.quantum.client.gui.widget.components.TextComponent;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class TitleButton extends Button<TitleButton> {
    private final TextComponent text;
    private final ColorComponent textColor;
    private NamespaceID icon;

    protected TitleButton() {
        this(200);
    }

    /**
     * @param width the width of the button
     */
    protected TitleButton(int width) {
        this(width, 21);
    }

    /**
     * @param width  the width of the button
     * @param height the height of the button
     */
    protected TitleButton(int width, int height) {
        super(width, height);

        this.text = this.register(id("text"), new TextComponent(null));
        this.textColor = this.register(id("text_color"), new ColorComponent(RgbColor.WHITE));
        this.icon = null;
    }

    public static TitleButton of(TextObject text) {
        return TitleButton.of(text, 200);
    }

    public static TitleButton of(TextObject text, int width) {
        return TitleButton.of(text, width, 21);
    }

    public static TitleButton of(TextObject text, int width, int height) {
        TitleButton button = new TitleButton(width, height);
        button.text.set(text);
        return button;
    }

    public static TitleButton of(String text) {
        return TitleButton.of(text, 200);
    }

    public static TitleButton of(String text, int width) {
        return TitleButton.of(text, width, 21);
    }

    public static TitleButton of(String text, int width, int height) {
        TitleButton button = new TitleButton(width, height);
        button.text.setRaw(text);
        return button;
    }

    @Override
    public TitleButton withPositioning(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public TitleButton withBounding(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    @Override
    public TitleButton withCallback(Callback<TitleButton> callback) {
        this.callback.set(callback);
        return this;
    }

    public TitleButton icon(NamespaceID icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        this.renderButton(renderer, texture, x, y);

        TextObject textObject = this.text.get();
        if (textObject != null) {
            if (this.icon != null) {
                renderer.blit(this.icon, x + size.width / 2f - 32, y - yOffset + size.width / 2f - 32, 64, 64, 0, 0, 64, 64, 64, 64);
            }

            renderer.line(x + 40, y - yOffset + this.size.height - 30, x + this.size.width - 40, y - yOffset + this.size.height - 30, (this.isEnabled ? this.textColor.get().withAlpha(0x80) : this.textColor.get().withAlpha(0x40)).toGdx());

            renderer.textCenter(textObject, x + this.size.width / 2, y - yOffset + (this.size.height - 20 - this.font.getLineHeight() + getButtonContentOffset()), this.isEnabled ? this.textColor.get() : this.textColor.get().withAlpha(0x80));
        }
    }

    @Override
    public String getName() {
        return "TitleButton";
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

    public TitleButton translation(String path, Object... args) {
        this.text.translate(path, args);
        return this;
    }

    public NamespaceID getIcon() {
        return icon;
    }
}
