package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import dev.ultreon.quantum.client.debug.Gizmo;

public class GizmoOutlineShader extends OutlineShader {
    public GizmoOutlineShader(GeomShaderConfig config, Renderable renderable) {
        super(renderable, config);

        register(Inputs.color, Setters.color);
    }

    public static class Inputs extends DefaultShader.Inputs {
        public final static BaseShader.Uniform color = new BaseShader.Uniform("u_color");

    }

    public static class Setters extends DefaultShader.Setters {
        public final static BaseShader.Setter color = new BaseShader.LocalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                if (renderable.userData instanceof Gizmo) {
                    Gizmo gizmo = (Gizmo) renderable.userData;
                    shader.set(inputID, gizmo.color);
                } else {
                    shader.set(inputID, 1f, 1f, 1f, 1f);
                }
            }
        };
    }

    @Override
    public void init() {
        try {
            super.init();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize gizmo outline shader", e);
        }
    }
}
