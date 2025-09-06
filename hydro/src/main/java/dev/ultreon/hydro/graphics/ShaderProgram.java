package dev.ultreon.hydro.graphics;

import dev.ultreon.hydro.core.*;
import org.joml.*;

public interface ShaderProgram extends Destroyable {
    void bind();

    default boolean isBindable() {
        return true;
    }

    void setUniformF(String name, float value);
    void setUniformF(String name, float x, float y);
    void setUniformF(String name, float x, float y, float z);
    void setUniformF(String name, float x, float y, float z, float w);
    void setUniformI(String name, int value);
    void setUniformI(String name, int x, int y);
    void setUniformI(String name, int x, int y, int z);
    void setUniformI(String name, int x, int y, int z, int w);

    void setUniform(String name, Quaternionf value);
    void setUniform(String name, Quaterniond value);

    void setUniform(String name, Vector2f value);
    void setUniform(String name, Vector3f value);
    void setUniform(String name, Vector4f value);

    void setUniform(String name, Vector2i value);
    void setUniform(String name, Vector3i value);
    void setUniform(String name, Vector4i value);

    void setUniform(String name, Vector2d value);
    void setUniform(String name, Vector3d value);
    void setUniform(String name, Vector4d value);

    void setUniform(String name, Matrix3f value);
    void setUniform(String name, Matrix4f value);

    void setUniform(String name, boolean value);
}
