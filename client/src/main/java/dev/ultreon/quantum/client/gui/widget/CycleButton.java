package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.ColorComponent;
import dev.ultreon.quantum.client.gui.widget.components.TextComponent;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class CycleButton<T> extends Button<CycleButton<T>> {
    private @Nullable TextObject label = null;
    private final TextComponent text;
    private final ColorComponent textColor;
    private T[] values;
    private Function<T, TextObject> formatter;
    private int cur;

    public CycleButton(int width, @Nullable MutableText name) {
        super(width, 21);

        this.text = this.register(id("text"), new TextComponent(TextObject.empty()));
        this.textColor = this.register(id("text_color"), new ColorComponent(RgbColor.WHITE));

        this.label = name;
        this.text.set(name);
    }

    public CycleButton(int width, int height, @Nullable MutableText name) {
        super(width, height);

        this.text = this.register(id("text"), new TextComponent(null));
        this.textColor = this.register(id("text_color"), new ColorComponent(RgbColor.WHITE));

        this.label = name;
        this.text.set(name);
    }

    public CycleButton() {
        super(200, 21);

        this.text = this.register(id("text"), new TextComponent(TextObject.empty()));
        this.textColor = this.register(id("text_color"), new ColorComponent(RgbColor.WHITE));
    }

    @Override
    public void revalidate() {
        TextObject lbl = this.label;
        if (lbl != null) {
            this.text().set(lbl.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        } else {
            this.text().set(this.formatter.apply(this.values[this.cur]));
        }

        super.revalidate();
    }

    @SafeVarargs
    public final CycleButton<T> values(T... values) {
        this.values = values;
        return this;
    }

    public final CycleButton<T> formatter(Function<T, TextObject> formatter) {
        this.formatter = formatter;
        if (values != null && values.length > 0 && this.label != null && this.formatter != null) {
            this.text().set(this.label.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        }
        return this;
    }

    @Override
    public CycleButton<T> withPositioning(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public CycleButton<T> withBounding(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    public TextComponent text() {
        return this.text;
    }

    public ColorComponent textColor() {
        return this.textColor;
    }

    @Override
    public boolean click() {
        this.cur = (this.cur + 1) % this.values.length;
        if (this.label != null) {
            this.text().set(this.label.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        } else {
            this.text().set(this.formatter.apply(this.values[this.cur]));
        }
        this.callback.call(this);
        return true;
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        this.renderButton(renderer, texture, x, y);

        TextObject textObject = this.text.get();
        if (textObject != null) {
            renderer.textCenter(textObject, x + this.size.width / 2, y - yOffset + (this.size.height / 2 - this.font.getLineHeight() + getButtonContentOffset()), this.isEnabled ? this.textColor.get() : this.textColor.get().withAlpha(0x80));
        }
    }

    public T getValue() {
        return this.values[this.cur];
    }

    public int getIndex() {
        return this.cur;
    }

    public @Nullable TextObject getLabel() {
        return this.label;
    }

    public String getRawLabel() {
        return this.label != null ? this.label.getText() : this.formatter.apply(this.values[this.cur]).getText();
    }

    public CycleButton<T> index(int index) {
        this.cur = Mth.clamp(index, 0, this.values.length - 1);
        return this;
    }

    public CycleButton<T> value(T o) {
        this.cur = Mth.clamp(ArrayUtils.indexOf(this.values, o), 0, this.values.length - 1);
        return this;
    }

    public CycleButton<T> label(TextObject label) {
        this.label = label;
        if (values != null && values.length > 0 && this.label != null && this.formatter != null) {
            this.text().set(this.label.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        }
        return this;
    }

    public CycleButton<T> label(String label) {
        this.label = TextObject.literal(label);
        if (values != null && values.length > 0 && this.formatter != null) {
            this.text().set(this.label.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        }

        return this;
    }

    public CycleButton<T> labelTranslation(String label, Object... args) {
        this.label = TextObject.translation(label, args);
        return this;
    }
}
