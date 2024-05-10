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
import dev.ultreon.quantum.util.RgbColor;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

@ApiStatus.NonExtendable
public class Label extends Widget {
    private final AlignmentComponent alignment;
    private final ColorComponent textColor;
    private final TextComponent text;
    private final ScaleComponent scale;

    public Label() {
        this(Alignment.LEFT, RgbColor.WHITE);
    }

    public Label(RgbColor textColor) {
        this(Alignment.LEFT, textColor);
    }

    public Label(Alignment alignment) {
        this(alignment, RgbColor.WHITE);
    }

    public Label(Alignment alignment, RgbColor textColor) {
        super(0, 0);
        this.alignment = this.register(id("alignment"), new AlignmentComponent(alignment));
        this.textColor = this.register(id("text_color"), new ColorComponent(textColor));
        this.text = this.register(id("text"), new TextComponent(null));
        this.scale = this.register(id("scale"), new ScaleComponent(1));
    }

    public static Label of(TextObject text) {
        Label label = new Label();
        label.text.set(text);
        return label;
    }

    public static Label of(String text) {
        Label label = new Label();
        label.text.setRaw(text);
        return label;
    }

    public static Label of() {
        Label label = new Label();
        label.text.set(TextObject.empty());
        return label;
    }

    @Override
    public Label position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public Label bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    public Label alignment(Alignment alignment) {
        this.alignment.set(alignment);
        return this;
    }

    public Label textColor(RgbColor textColor) {
        this.textColor.set(textColor);
        return this;
    }

    public Label scale(int scale) {
        this.scale.set(scale);
        return this;
    }

    @Override
    public void renderBackground(Renderer renderer, float deltaTime) {
        this.size.idt();
        int scale = this.scale.get();
        var text = this.text.get();
        var textColor = this.textColor.get();

        if (text == null) return;

        switch (this.alignment.get()) {
            case LEFT -> renderer.textLeft(text, scale, this.pos.x, this.pos.y, textColor);
            case CENTER -> renderer.textCenter(text, scale, this.pos.x, this.pos.y, textColor);
            case RIGHT -> renderer.textRight(text, scale, this.pos.x, this.pos.y, textColor);
        }
    }

    @Override
    public String getName() {
        return "Label";
    }

    public ScaleComponent scale() {
        return this.scale;
    }

    public TextComponent text() {
        return this.text;
    }

    public AlignmentComponent alignment() {
        return this.alignment;
    }
}
