package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.ColorComponent;
import dev.ultreon.quantum.util.RgbColor;
import org.checkerframework.common.value.qual.IntRange;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;

public class Rectangle extends Widget {
    private final ColorComponent backgroundColor;

    public Rectangle(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);

        this.backgroundColor = this.register(id("background_color"), new ColorComponent(RgbColor.BLACK.withAlpha(0x80)));
    }

    public Rectangle() {
        this(0, 0, 0, 0);
    }

    public static Rectangle of(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }

    public static Rectangle create() {
        return new Rectangle();
    }

    public Rectangle backgroundColor(RgbColor color) {
        this.backgroundColor.set(color);
        return this;
    }

    @Override
    public Rectangle position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public Rectangle bounds(Supplier<Bounds> bounds) {
        this.onRevalidate(widget -> this.setBounds(bounds.get()));
        return this;
    }

    public ColorComponent backgroundColor() {
        return this.backgroundColor;
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
//        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//        renderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//        renderer.getBatch().setTransformMatrix(renderer.getBatch().getTransformMatrix().translate(0, 0, 10));
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, this.backgroundColor.get());
//        renderer.getBatch().setTransformMatrix(renderer.getBatch().getTransformMatrix().translate(0, 0, -10));
//        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        return true;
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        return true;
    }

    @Override
    public String getName() {
        return "Panel";
    }
}
