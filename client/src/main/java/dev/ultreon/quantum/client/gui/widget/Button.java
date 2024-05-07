package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.CallbackComponent;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.util.Color;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public abstract class Button<T extends Button<T>> extends Widget {
    protected final CallbackComponent<T> callback;
    private Type type;
    private boolean pressed;

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
        int u;
        if (this.enabled) u = this.isWithinBounds(mouseX, mouseY) ? 21 : 0;
        else u = 42;
        int v = this.isPressed() ? 21 : 0;

        renderer.blitColor(Color.WHITE)
                .blit(texture, x, y, 7, 7, u, v, 7, 7)
                .blit(texture, x + 7, y, this.size.width - 14, 7, 7 + u, v, 7, 7)
                .blit(texture, x + this.size.width - 7, y, 7, 7, 14 + u, v, 7, 7)
                .blit(texture, x, y + 7, 7, this.size.height - 14, u, 7 + v, 7, 7)
                .blit(texture, x + 7, y + 7, this.size.width - 14, this.size.height - 14, 7 + u, 7 + v, 7, 7)
                .blit(texture, x + this.size.width - 7, y + 7, 7, this.size.height - 14, 14 + u, 7 + v, 7, 7)
                .blit(texture, x, y + this.size.height - 7, 7, 7, u, 14 + v, 7, 7)
                .blit(texture, x + 7, y + this.size.height - 7, this.size.width - 14, 7, 7 + u, 14 + v, 7, 7)
                .blit(texture, x + this.size.width - 7, y + this.size.height - 7, 7, 7, 14 + u, 14 + v, 7, 7);
    }

    @ApiStatus.OverrideOnly
    public boolean click() {
        if (!this.enabled) return false;

        CallbackComponent<? extends Button> callback = this.callback;
        if (callback == null) {
            return true;
        }
        callback.call0(this);
        return false;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        return !this.click();
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        if (!this.enabled) return false;

        if (!this.pressed) {
            this.client.playSound(SoundEvents.BUTTON_PRESS, 1.0f);
        }

        this.pressed = true;
        return super.mousePress(x, y, button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        if (this.pressed) {
            this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
        }

        this.pressed = false;
        return super.mouseRelease(x, y, button);
    }

    public boolean isPressed() {
        return this.pressed && this.enabled;
    }

    @Override
    public abstract Button<T> position(Supplier<Position> position);

    @Override
    public abstract Button<T> bounds(Supplier<Bounds> position);

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
        LIGHT(3, 2),
        DANGER(1, 0),
        WARNING(3, 1),
        SUCCESS(2, 0),
        PRIMARY(3, 0);

        private final int xOffset;
        private final int yOffset;

        Type(int xOffset, int yOffset) {

            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }
}
