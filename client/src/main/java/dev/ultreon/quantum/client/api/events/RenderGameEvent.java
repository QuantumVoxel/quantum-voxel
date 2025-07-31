package dev.ultreon.quantum.client.api.events;

import com.badlogic.gdx.math.GridPoint2;
import dev.ultreon.quantum.client.GameRenderer;
import dev.ultreon.quantum.client.gui.Renderer;

public abstract class RenderGameEvent implements GuiRenderEvent {
    private final Renderer renderer;
    private final GameRenderer gameRenderer;
    private final GridPoint2 drawOffset;
    private final int width;
    private final int height;
    private final float deltaTime;

    public RenderGameEvent(Renderer renderer, GameRenderer gameRenderer, GridPoint2 drawOffset, int width, int height, float deltaTime) {
        this.renderer = renderer;
        this.gameRenderer = gameRenderer;
        this.drawOffset = drawOffset;
        this.width = width;
        this.height = height;
        this.deltaTime = deltaTime;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public GridPoint2 getDrawOffset() {
        return drawOffset;
    }

    public GameRenderer getGameRenderer() {
        return gameRenderer;
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public float getDeltaTime() {
        return deltaTime;
    }

    public static class Pre extends RenderGameEvent {
        public Pre(Renderer renderer, GameRenderer gameRenderer, GridPoint2 drawOffset, int width, int height, float deltaTime) {
            super(renderer, gameRenderer, drawOffset, width, height, deltaTime);
        }
    }

    public static class Post extends RenderGameEvent {
        public Post(Renderer renderer, GameRenderer gameRenderer, GridPoint2 drawOffset, int width, int height, float deltaTime) {
            super(renderer, gameRenderer, drawOffset, width, height, deltaTime);
        }
    }
}
