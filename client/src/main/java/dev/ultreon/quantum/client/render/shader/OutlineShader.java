package dev.ultreon.quantum.client.render.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

public class OutlineShader extends DefaultShader {
    public final int u_resolution;

    public OutlineShader(Renderable renderable) {
        super(renderable);

        this.u_resolution = this.register(Inputs.u_resolution, Setters.resolution);
    }

    public static class Inputs extends DefaultShader.Inputs {
        public final static Uniform u_resolution = new Uniform("u_resolution");
    }


    public static class Setters extends DefaultShader.Setters {
        public final static Setter resolution = new LocalSetter() {
            @Override
            public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
            }
        };
    }
}
