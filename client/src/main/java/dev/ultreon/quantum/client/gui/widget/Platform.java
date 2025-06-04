package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;

import java.util.function.Supplier;

public class Platform extends Widget {
    private float depth = 3f;

    public Platform(int x, int y, int width, int height) {
        super(width, height);
    }

    public Platform() {
        this(0, 0, 0, 0);
    }

    public static Platform of(int x, int y, int width, int height) {
        return new Platform(x, y, width, height);
    }

    public static Platform create() {
        return new Platform();
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public Platform depth(float depth) {
        this.depth = depth;
        return this;
    }

    @Override
    public Platform withPositioning(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public Platform withBounding(Supplier<Bounds> bounds) {
        this.onRevalidate(widget -> this.setBounds(bounds.get()));
        return this;
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        renderer.drawPlatform(pos.x, pos.y, size.width, size.height, depth);
    }

    @Override
    public String getName() {
        return "Panel";
    }
}
