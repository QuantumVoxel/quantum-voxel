package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import dev.ultreon.quantum.client.debug.Gizmo;

public class GizmoShader extends ModelViewShader {
    public GizmoShader(Renderable renderable) {
        super(renderable);

        register(Inputs.color, Setters.color);
    }

    public GizmoShader(Renderable renderable, GeomShaderConfig config) {
        super(renderable, config);

        register(Inputs.color, Setters.color);
    }

    public static class Inputs extends DefaultShader.Inputs {
        public final static BaseShader.Uniform color = new BaseShader.Uniform("u_color");
        public final static BaseShader.Uniform diffuseTexture = new BaseShader.Uniform("u_framebuffer");
    }

    public static class Setters extends DefaultShader.Setters {
        public final static BaseShader.Setter color = new BaseShader.LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                switch (renderable.userData) {
                    case Gizmo gizmo -> shader.set(inputID, gizmo.color);
                    case null, default -> shader.set(inputID, 1f, 1f, 1f, 1f);
                }
            }
        };

        public final static Setter framebuffer = new LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, 12);
            }
        };
    }
}
