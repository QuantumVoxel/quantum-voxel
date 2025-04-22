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

    protected Button(int width, int height) {
        this(width, height, Type.DARK);
    }

    protected Button(int width, int height, Type type) {
        super(width, height);
        this.type = type;

        this.callback = this.register(id("callback"), new CallbackComponent<>(it -> {

        }));
    }

    protected void renderButton(Renderer renderer, Texture texture, int x, int y) {
        if (!isHovered() && pressed) {
            this.pressed = false;
        }

        calculateOffset();

        renderer.fill(x + 1, y + this.size.height, this.size.width - 2, 1, Renderer.DARK_TRANSPARENT);

        if (!isEnabled) {
            renderer.drawDisabledPlatform(pos.x, pos.y, size.width, size.height, yOffset);
        } else if (isHovered) {
            renderer.drawHighlightPlatform(pos.x, pos.y, size.width, size.height, yOffset);
        } else {
            renderer.drawPlatform(pos.x, pos.y, size.width, size.height, yOffset);
        }

        if (!pressed && wasPressed && !isHovered) {
            this.wasPressed = false;
            this.client.playSound(SoundEvents.BUTTON_RELEASE, 1.0f);
        }
    }

    private void calculateOffset() {
        float yOffsetGoal = calculateGoal();
        if (yOffset > yOffsetGoal) {
            if (yOffset < yOffsetGoal + 0.3f) yOffset = yOffsetGoal;
            yOffset -= (yOffset - yOffsetGoal) * Gdx.graphics.getDeltaTime() * 8f;
        } else if (yOffset < yOffsetGoal) {
            if (yOffset > yOffsetGoal - 0.3f) yOffset = yOffsetGoal;
            yOffset += (yOffsetGoal - yOffset) * Gdx.graphics.getDeltaTime() * 8f;
        }
    }

    private float calculateGoal() {
        float yOffsetGoal;
        if (type.inset) {
            yOffsetGoal = isHovered && isEnabled ? 2f : 0f;
            if (this.pressed && isEnabled)
                yOffsetGoal = -2f;
        } else {
            yOffsetGoal = isHovered && isEnabled ? 4f : 2f;
            if (this.pressed && isEnabled)
                yOffsetGoal = 1f;
        }
        return yOffsetGoal;
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
        DARK(0, false),
        LIGHT(1, false),
        DARK_EMBED(0, true),
        LIGHT_EMBED(1, true);

        private final int xOffset;
        private final boolean inset;

        Type(int xOffset, boolean inset) {
            this.xOffset = xOffset;
            this.inset = inset;
        }
    }
}
