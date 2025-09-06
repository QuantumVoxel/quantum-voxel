package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector4;
import dev.ultreon.quantum.client.render.context.GameShader;
import dev.ultreon.quantum.client.render.context.ShaderContext;
import org.jetbrains.annotations.NotNull;

public abstract class BaseGameShader extends DefaultShader implements GameShader {
    protected final ShaderContext context = new ShaderContext(this);

    public BaseGameShader(Renderable renderable) {
        super(renderable);
    }

    public BaseGameShader(Renderable renderable, Config config) {
        super(renderable, config);
    }

    public BaseGameShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix);
    }

    public BaseGameShader(Renderable renderable, Config config, String prefix, String vertexShader, String fragmentShader) {
        super(renderable, config, prefix, vertexShader, fragmentShader);
    }

    public BaseGameShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        super(renderable, config, shaderProgram);
    }

    public ShaderProgram getShaderProgram() {
        return this.program;
    }

    @Override
    public boolean set(int location, @NotNull Texture texture) {
        int bind = this.context.getTextureBinder().bind(texture);
        if (bind == -1) {
            return false;
        }

        return this.set(location, bind);
    }

    @Override
    public boolean set(int location, boolean value) {
        return this.set(location, value ? 1 : 0);
    }

    @Override
    public boolean set(int location, @NotNull Quaternion value) {
        return this.set(location, value.x, value.y, value.z, value.w);
    }

    @Override
    public boolean set(int location, @NotNull Vector4 value) {
        return this.set(location, value.x, value.y, value.z, value.w);
    }

    @Override
    public boolean set(int location, @NotNull GridPoint2 value) {
        return this.set(location, value.x, value.y);
    }

    @Override
    public boolean set(int location, @NotNull GridPoint3 value) {
        return this.set(location, value.x, value.y, value.z);
    }

    @Override
    public @NotNull ShaderContext getContext() {
        return this.context;
    }
}
