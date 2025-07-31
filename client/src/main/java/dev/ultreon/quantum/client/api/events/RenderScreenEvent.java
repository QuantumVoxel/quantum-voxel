package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import org.jetbrains.annotations.Nullable;

public abstract class RenderScreenEvent implements ScreenEvent, GuiRenderEvent {
    @Nullable
    private final Screen screen;
    private final Renderer renderer;
    private final float mouseX;
    private final float mouseY;
    private final float deltaTime;

    public RenderScreenEvent(@Nullable Screen screen, Renderer renderer, float mouseX, float mouseY, float deltaTime) {
        this.screen = screen;
        this.renderer = renderer;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.deltaTime = deltaTime;
    }

    @Override
    public @Nullable Screen getScreen() {
        return screen;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    public float getMouseX() {
        return mouseX;
    }

    public float getMouseY() {
        return mouseY;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public static class Pre extends RenderScreenEvent {
        public Pre(@Nullable Screen screen, Renderer renderer, float mouseX, float mouseY, float deltaTime) {
            super(screen, renderer, mouseX, mouseY, deltaTime);
        }
    }

    public static class Post extends RenderScreenEvent {
        public Post(@Nullable Screen screen, Renderer renderer, float mouseX, float mouseY, float deltaTime) {
            super(screen, renderer, mouseX, mouseY, deltaTime);
        }
    }
}
