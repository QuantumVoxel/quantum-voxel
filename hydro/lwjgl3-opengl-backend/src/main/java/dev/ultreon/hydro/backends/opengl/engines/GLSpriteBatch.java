package dev.ultreon.hydro.backends.opengl.engines;

import dev.ultreon.hydro.Hydro;
import dev.ultreon.hydro.util.Color;
import dev.ultreon.hydro.graphics.SpriteBatch;
import dev.ultreon.hydro.graphics.Texture;
import org.joml.Matrix4f;

public class GLSpriteBatch implements SpriteBatch {
    private final Color color = new Color(1, 1, 1, 1);
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f transformation = new Matrix4f();

    @Override
    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    @Override
    public void setColor(float r, float g, float b) {
        this.color.set(r, g, b);
    }

    @Override
    public void setProjectionMatrix(Matrix4f projection) {
        this.projection.set(projection);
    }

    @Override
    public void setTransformMatrix(Matrix4f transform) {
        this.transformation.set(transform);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {

    }

    @Override
    public void draw(Texture texture, float x, float y) {

    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {

    }

    @Override
    public void begin() {

    }

    @Override
    public void end() {

    }

    @Override
    public void destroy() {

    }
}
