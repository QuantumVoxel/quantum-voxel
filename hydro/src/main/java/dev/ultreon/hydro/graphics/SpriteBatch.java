package dev.ultreon.hydro.graphics;

import dev.ultreon.hydro.Usable;
import dev.ultreon.hydro.core.Destroyable;
import org.joml.Matrix4f;

public interface SpriteBatch extends Destroyable, Usable {
    void setColor(float r, float g, float b, float a);
    void setColor(float r, float g, float b);

    void setProjectionMatrix(Matrix4f projection);

    void setTransformMatrix(Matrix4f transform);

    void draw(Texture texture, float x, float y, float width, float height);
    void draw(Texture texture, float x, float y);
    void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2);
}
