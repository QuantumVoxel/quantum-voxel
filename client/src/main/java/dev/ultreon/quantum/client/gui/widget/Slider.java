package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.CallbackComponent;
import dev.ultreon.quantum.client.gui.widget.components.RangedValueComponent;
import dev.ultreon.quantum.client.gui.widget.components.TextComponent;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.function.Supplier;

public class Slider extends Widget {
    private static final NamespaceID TEXTURE = NamespaceID.of("textures/gui/slider.png");
    private final CallbackComponent<Slider> callback;
    private final RangedValueComponent value;
    private final TextComponent text;
    private boolean isHolding;
    private int holdStart;
    private int originalValue;

    public Slider(int value, int min, int max) {
        this(200, value, min, max);
    }

    public Slider(int width, int value, int min, int max) {
        super(width, 21);

        this.callback = this.register(NamespaceID.of("callback"), new CallbackComponent<>(it -> {
        }));
        this.value = this.register(NamespaceID.of("value"), new RangedValueComponent(value, min, max));
        this.text = this.register(NamespaceID.of("text"), new TextComponent());
    }

    public static Slider of(int min, int max) {
        return new Slider(min, min, max);
    }

    @Override
    public Slider withPositioning(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public Slider withBounding(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        int thumbX = this.pos.x + (this.size.width - 14) * (this.value.get() - this.value.min()) / (this.value.max() - this.value.min()) + 2;

        // Track
        renderer.blit(Slider.TEXTURE, this.pos.x, this.pos.y, 7, 21, 0, 0, 7, 21);
        renderer.blit(Slider.TEXTURE, this.pos.x + 7, this.pos.y, this.size.width - 14, 21, 7, 0, 7, 21);
        renderer.blit(Slider.TEXTURE, this.pos.x + this.size.width - 7, this.pos.y, 7, 21, 14, 0, 7, 21);

        // Thumb
        if (isHovered || this.isHolding) {
            renderer.blit(Slider.TEXTURE, thumbX, this.pos.y, 10, 19, 33, 0, 10, 19);
        } else {
            renderer.blit(Slider.TEXTURE, thumbX, this.pos.y, 10, 19, 22, 0, 10, 19);
        }

        // Text
        TextObject textObject = this.text.get();
        if (textObject != null) {
            renderer.textLeft(textObject.copy().append(": ").append(this.value.get()), this.pos.x + this.size.width + 5, this.pos.y + 5, true);
        } else {
            renderer.textLeft(String.valueOf(this.value.get()), this.pos.x + this.size.width + 5, this.pos.y + 5, true);
        }

        super.renderWidget(renderer, deltaTime);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        int newValue = this.originalValue + (this.value.max() - this.value.min()) * (mouseX - this.holdStart) / (this.size.width - 14);
        if (this.isHolding && newValue >= this.value.min() && newValue <= this.value.max() && newValue != this.value.get()) {
            this.value.set(newValue);
            this.callback.call(this);

            return true;
        }

        return false;
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        int thumbX = this.pos.x + (this.size.width - 14) * (this.value.get() - this.value.min()) / (this.value.max() - this.value.min()) + 2;

        if (mouseX < thumbX || mouseX > thumbX + 10) {
            // Set the value to the mouse position
            this.value.set((mouseX - this.pos.x - 2) * (this.value.max() - this.value.min()) / (this.size.width - 14) + this.value.min());
            this.callback.call(this);
            return true;
        }

        this.originalValue = this.value.get();
        this.holdStart = mouseX;
        this.isHolding = true;

        return true;
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        this.isHolding = false;

        return super.mouseRelease(mouseX, mouseY, button);
    }

    public Slider setCallback(Callback<Slider> callback) {
        this.callback.set(callback);
        return this;
    }

    public Slider value(int value) {
        this.value.set(value);
        return this;
    }

    public Slider text(TextObject text) {
        this.text.set(text);
        return this;
    }

    public Slider text(String text) {
        this.text.setRaw(text);
        return this;
    }

    public CallbackComponent<Slider> callback() {
        return this.callback;
    }

    public RangedValueComponent value() {
        return this.value;
    }

    public TextComponent text() {
        return this.text;
    }
}
