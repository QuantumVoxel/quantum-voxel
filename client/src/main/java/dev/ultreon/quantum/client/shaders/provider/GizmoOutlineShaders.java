package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.GizmoOutlineShader;
import dev.ultreon.quantum.client.shaders.Shaders;

public class GizmoOutlineShaders extends OutlineShaderProvider {
    public GizmoOutlineShaders(ResourceFileHandle resourceFileHandle, ResourceFileHandle resourceFileHandle1) {
        super(resourceFileHandle, resourceFileHandle1);
    }

    @Override
    public Shader createShader(Renderable renderable) {
        GizmoOutlineShader gizmoOutlineShader = new GizmoOutlineShader(new GeomShaderConfig(
                QuantumClient.resource(QuantumClient.id("shaders/gizmo_outline.vert")).readString(),
                QuantumClient.resource(QuantumClient.id("shaders/gizmo_outline.frag")).readString(),
                QuantumClient.resource(QuantumClient.id("shaders/gizmo_outline.geom")).readString()
        ), renderable);

        Shaders.checkShaderCompilation(gizmoOutlineShader.program, "GizmoOutlineShader");
        return gizmoOutlineShader;
    }
}
