package dev.ultreon.hydro.backends.libgdx.graphics;

import com.badlogic.gdx.math.Matrix4;
import dev.ultreon.hydro.graphics.SpriteBatch;
import dev.ultreon.hydro.graphics.Texture;
import org.joml.Matrix4f;

public class GdxSpriteBatch extends com.badlogic.gdx.graphics.g2d.SpriteBatch implements SpriteBatch {
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 transformMatrix = new Matrix4();

    @Override
    public void setColor(float r, float g, float b) {
        this.setColor(r, g, b, 1);
    }

    @Override
    public void setProjectionMatrix(Matrix4f projection) {
        projection.get(projectionMatrix.val);
        setProjectionMatrix(projectionMatrix);
    }

    @Override
    public void setTransformMatrix(Matrix4f transform) {
        transform.get(transformMatrix.val);
        setTransformMatrix(transformMatrix);
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {
        draw(texture, x, y, width, height, 0, 0, 1, 1);
    }

    @Override
    public void draw(Texture texture, float x, float y) {
        draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        super.draw((GdxTexture) texture, x, y, width, height, u, v, u2, v2);
    }

    @Override
    public void destroy() {
        dispose();
    }
}
