package dev.ultreon.quantum.client.gui.widget;

import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import org.checkerframework.common.value.qual.IntRange;

import java.util.function.Supplier;

public class Panel extends Widget {
    public Panel(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
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
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.renderFrame(pos.x, pos.y, size.width, size.height);
    }

    @Override
    public String getName() {
        return "Panel";
    }
}
