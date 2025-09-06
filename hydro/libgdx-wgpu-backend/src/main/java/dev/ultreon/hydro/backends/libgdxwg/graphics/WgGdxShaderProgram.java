package dev.ultreon.hydro.backends.libgdxwg.graphics;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import dev.ultreon.hydro.graphics.ShaderProgram;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;

@ApiStatus.Experimental
public class WgGdxShaderProgram implements ShaderProgram {
    private final WgShaderProgram program;

    public WgGdxShaderProgram(String name, String shader) {
        this.program = new WgShaderProgram(name, shader, null);
    }

    @Override
    public void bind() {
        throw new UnsupportedOperationException("Unsupported in WebGPU");
    }

    @Override
    public boolean isBindable() {
        return false;
    }

    @Override
    public void setUniformF(String name, float value) {

    }

    @Override
    public void setUniformF(String name, float x, float y) {

    }

    @Override
    public void setUniformF(String name, float x, float y, float z) {

    }

    @Override
    public void setUniformF(String name, float x, float y, float z, float w) {

    }

    @Override
    public void setUniformI(String name, int value) {

    }

    @Override
    public void setUniformI(String name, int x, int y) {

    }

    @Override
    public void setUniformI(String name, int x, int y, int z) {

    }

    @Override
    public void setUniformI(String name, int x, int y, int z, int w) {

    }

    @Override
    public void setUniform(String name, Quaternionf value) {

    }

    @Override
    public void setUniform(String name, Quaterniond value) {

    }

    @Override
    public void setUniform(String name, Vector2f value) {

    }

    @Override
    public void setUniform(String name, Vector3f value) {

    }

    @Override
    public void setUniform(String name, Vector4f value) {

    }

    @Override
    public void setUniform(String name, Vector2i value) {

    }

    @Override
    public void setUniform(String name, Vector3i value) {

    }

    @Override
    public void setUniform(String name, Vector4i value) {

    }

    @Override
    public void setUniform(String name, Vector2d value) {

    }

    @Override
    public void setUniform(String name, Vector3d value) {

    }

    @Override
    public void setUniform(String name, Vector4d value) {

    }

    @Override
    public void setUniform(String name, Matrix3f value) {

    }

    @Override
    public void setUniform(String name, Matrix4f value) {

    }

    @Override
    public void setUniform(String name, boolean value) {

    }

    @Override
    public void destroy() {
        program.dispose();
    }
}
