package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.GamePlatform;
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
    public final CallbackComponent<T> callback;
    public Type type;
    public boolean pressed;
    public boolean wasPressed;
    private final Color tmp = new Color();
    protected float yOffset = 0f;

    protected Button(@IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        this(width, height, Type.DARK);
    }

    protected Button(@IntRange(from = 0) int width, @IntRange(from = 0) int height, Type type) {
        super(width, height);
        this.type = type;

        this.callback = this.register(id("callback"), new CallbackComponent<>(it -> {

        }));
    }

    protected void renderButton(Renderer renderer, Texture texture, int x, int y) {
        if (!isHovered() && pressed) {
            this.pressed = false;
        }

        if (shouldBeTransparent()) {
            Color color = tmp;
            if (isHovered) {
                color.set(1, 1, 1, 0.25f);
            } else if (this.pressed) {
                color.set(1, 1, 1, 0.4f);
            } else if (this.isEnabled) {
                color.set(1, 1, 1, 0.1f);
            } else {
                color.set(1, 1, 1, 0.05f);
            }
            renderer.fill(x, y, this.size.width, this.size.height, color);
            return;
        }

        int u;
        if (this.isEnabled) u = isHovered ? 21 : 0;
        else u = 42;
        int v = 0;

        u += 63 * type.xOffset;
        v += 42 * type.yOffset;

        float yOffsetGoal = isHovered && isEnabled ? 2f : 0f;
        if (this.pressed && isEnabled) {
            yOffsetGoal = -1f;
        }
        if (yOffset > yOffsetGoal) {
            yOffset -= (yOffset - yOffsetGoal) * Gdx.graphics.getDeltaTime() * 8f;
        } else if (yOffset < yOffsetGoal) {
            yOffset += (yOffsetGoal - yOffset) * Gdx.graphics.getDeltaTime() * 8f;
        }

        renderer.fill(x + 1, y + this.size.height, this.size.width - 2, 1, Renderer.DARK_TRANSPARENT);

        renderer.draw9Slice(texture, x, y - yOffset, this.size.width, this.size.height - 3, u, v, 21, 18, 3, 256, 256);
        renderer.draw9Slice(texture, x, y + this.size.height - yOffset - 3, this.size.width, 3 + yOffset, u, v + 18, 21, 3, 1, 256, 256);
        if (!pressed && wasPressed && !isHovered) {
            this.wasPressed = false;
            this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
        }
    }

    private boolean shouldBeTransparent() {
        return GamePlatform.get().hasBackPanelRemoved() && client.world == null && !client.renderWorld && client.worldRenderer == null;
    }

    @ApiStatus.OverrideOnly
    public boolean click() {
        if (!this.isEnabled) return false;
        if (!wasPressed) return false;

        this.wasPressed = false;

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
        if (!this.isEnabled) return false;

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
        return this.pressed && this.isEnabled;
    }

    @Override
    public abstract Button<T> position(Supplier<Position> position);

    @Override
    public abstract Button<T> bounds(Supplier<Bounds> position);

    @SuppressWarnings("unchecked")
    public T setCallback(Callback<T> callback) {
        this.callback.set(callback);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setType(Type type) {
        this.type = type;
        return (T) this;
    }

    protected float getButtonContentOffset() {
        return shouldBeTransparent() ? 1 : 0;
    }

    public enum Type {
        DARK(0, 0),
        LIGHT(1, 0),
        @Deprecated
        DARK_EMBED(0, 0),
        @Deprecated
        LIGHT_EMBED(1, 0);

        private final int xOffset;
        private final int yOffset;

        Type(int xOffset, int yOffset) {

            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }
}
