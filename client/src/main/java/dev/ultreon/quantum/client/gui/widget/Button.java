package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.CallbackComponent;
import dev.ultreon.quantum.sound.event.SoundEvents;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public abstract class Button<T extends Button<T>> extends Widget {
    protected final CallbackComponent<T> callback;
    private Type type;
    private boolean pressed;
    private boolean wasPressed;

    protected Button(@IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        this(width, height, Type.DARK);
    }

    protected Button(@IntRange(from = 0) int width, @IntRange(from = 0) int height, Type type) {
        super(width, height);
        this.type = type;

        this.callback = this.register(id("callback"), new CallbackComponent<>(it -> {

        }));
    }

    protected void renderButton(Renderer renderer, int mouseX, int mouseY, Texture texture, int x, int y) {
        if (!isHovered() && pressed) {
            this.pressed = false;
        }


        int u;
        if (this.enabled) u = this.isWithinBounds(mouseX, mouseY) ? 21 : 0;
        else u = 42;
        int v = this.isPressed() ? 21 : 0;

        u += 63 * type.xOffset;
        v += 42 * type.yOffset;

        renderer.draw9Slice(texture, x, y, this.size.width, this.size.height, u, v, 21, 21, 5, 256, 256);
        if (!isPressed() && wasPressed) {
            this.wasPressed = false;
            this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
        }
    }

    @ApiStatus.OverrideOnly
    public boolean click() {
        if (!this.enabled) return false;
        if (!wasPressed) return false;

        this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);

        CallbackComponent<? extends Button<?>> callback = this.callback;
        if (callback == null) {
            return false;
        }
        callback.call0(this);
        return true;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        return !this.click();
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        if (!this.enabled) return false;

        this.pressed = true;
        this.wasPressed = true;

        this.client.playSound(SoundEvents.BUTTON_PRESS, 1.0f);

        super.mousePress(x, y, button);
        return true;
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        this.pressed = false;
        super.mouseRelease(x, y, button);
        return true;
    }

    public boolean isPressed() {
        return this.pressed && this.enabled;
    }

    @Override
    public abstract Button<T> position(Supplier<Position> position);

    @Override
    public abstract Button<T> bounds(Supplier<Bounds> position);

    @SuppressWarnings("unchecked")
    public T callback(Callback<T> callback) {
        this.callback.set(callback);
        return (T) this;
    }

    public CallbackComponent<T> callback() {
        return this.callback;
    }

    @SuppressWarnings("unchecked")
    public T type(Type type) {
        this.type = type;
        return (T) this;
    }

    public Type type() {
        return this.type;
    }

    public enum Type {
        DARK(0, 0),
        LIGHT(1, 0),
        DARK_EMBED(2, 0),
        LIGHT_EMBED(3, 0);

        private final int xOffset;
        private final int yOffset;

        Type(int xOffset, int yOffset) {

            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }
}
