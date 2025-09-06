package dev.ultreon.hydro.backends.libgdx.graphics;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import dev.ultreon.hydro.graphics.ShaderProgram;
import dev.ultreon.hydro.graphics.ShaderPart;
import org.joml.*;

public class GdxShaderProgram implements ShaderProgram {
    private final com.badlogic.gdx.graphics.glutils.ShaderProgram program;
    private final Matrix3 mat3 = new Matrix3();
    private final Matrix4 mat4 = new Matrix4();

    public GdxShaderProgram(String vertexPath, String fragmentPath) {
        this.program = new com.badlogic.gdx.graphics.glutils.ShaderProgram(vertexPath, fragmentPath);
    }

    public GdxShaderProgram(ShaderPart[] parts) {
        String vertex = null;
        String fragment = null;
        for (ShaderPart part : parts) {
            switch (part.type) {
                case Vertex:
                    if (vertex != null) {
                        throw new IllegalArgumentException("Vertex shader already defined");
                    }
                    vertex = part.source;
                    break;
                case Fragment:
                    if (fragment != null) {
                        throw new IllegalArgumentException("Fragment shader already defined");
                    }
                    fragment = part.source;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported shader part type: " + part.type);
            }
        }

        if (vertex == null) vertex = "#version 330 core\nvoid main() {}";
        if (fragment == null) fragment = "#version 330 core\nvoid main() {}";

        this.program = new com.badlogic.gdx.graphics.glutils.ShaderProgram(vertex, fragment);
    }

    @Override
    public void bind() {
        program.bind();
    }

    @Override
    public void setUniformF(String name, float value) {
        program.setUniformf(name, value);
    }

    @Override
    public void setUniformF(String name, float x, float y) {
        program.setUniformf(name, x, y);
    }

    @Override
    public void setUniformF(String name, float x, float y, float z) {
        program.setUniformf(name, x, y, z);
    }

    @Override
    public void setUniformF(String name, float x, float y, float z, float w) {
        program.setUniformf(name, x, y, z, w);
    }

    @Override
    public void setUniformI(String name, int value) {
        program.setUniformi(name, value);
    }

    @Override
    public void setUniformI(String name, int x, int y) {
        program.setUniformi(name, x, y);
    }

    @Override
    public void setUniformI(String name, int x, int y, int z) {
        program.setUniformi(name, x, y, z);
    }

    @Override
    public void setUniformI(String name, int x, int y, int z, int w) {
        program.setUniformi(name, x, y, z, w);
    }

    @Override
    public void setUniform(String name, Quaternionf value) {
        program.setUniformf(name, value.x, value.y, value.z, value.w);
    }

    @Override
    public void setUniform(String name, Quaterniond value) {
        throw new UnsupportedOperationException("Doubles are not supported in LibGDX");
    }

    @Override
    public void setUniform(String name, Vector2f value) {
        program.setUniformf(name, value.x, value.y);
    }

    @Override
    public void setUniform(String name, Vector3f value) {
        program.setUniformf(name, value.x, value.y, value.z);
    }

    @Override
    public void setUniform(String name, Vector4f value) {
        program.setUniformf(name, value.x, value.y, value.z, value.w);
    }

    @Override
    public void setUniform(String name, Vector2i value) {
        program.setUniformi(name, value.x, value.y);
    }

    @Override
    public void setUniform(String name, Vector3i value) {
        program.setUniformi(name, value.x, value.y, value.z);
    }

    @Override
    public void setUniform(String name, Vector4i value) {
        program.setUniformi(name, value.x, value.y, value.z, value.w);
    }

    @Override
    public void setUniform(String name, Vector2d value) {
        throw new UnsupportedOperationException("Doubles are not supported in LibGDX");
    }

    @Override
    public void setUniform(String name, Vector3d value) {
        throw new UnsupportedOperationException("Doubles are not supported in LibGDX");
    }

    @Override
    public void setUniform(String name, Vector4d value) {
        throw new UnsupportedOperationException("Doubles are not supported in LibGDX");
    }

    @Override
    public void setUniform(String name, Matrix3f value) {
        program.setUniformMatrix(name, mat3.set(value.get(mat3.getValues())));
    }

    @Override
    public void setUniform(String name, Matrix4f value) {
        program.setUniformMatrix(name, mat4.set(value.get(mat4.getValues())));
    }

    @Override
    public void setUniform(String name, boolean value) {
        program.setUniformi(name, value ? 1 : 0);
    }

    @Override
    public void destroy() {
        program.dispose();
    }
}
