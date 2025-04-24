package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;

import java.util.function.Supplier;

public class Panel extends Widget {
    private float depth = 3f;

    public Panel(int x, int y, int width, int height) {
        super(width, height);
    }

    public Panel() {
        this(0, 0, 0, 0);
    }

    public static Panel of(int x, int y, int width, int height) {
        return new Panel(x, y, width, height);
    }

    public static Panel create() {
        return new Panel();
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public Panel depth(float depth) {
        this.depth = depth;
        return this;
    }

    @Override
    public Panel position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public Panel bounds(Supplier<Bounds> bounds) {
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
