package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.GizmoOutlineShader;
import dev.ultreon.quantum.client.shaders.Shaders;

public class GizmoOutlineShaders extends OutlineShaderProvider {
    public GizmoOutlineShaders(FileHandle resourceFileHandle, FileHandle resourceFileHandle1) {
        super(resourceFileHandle, resourceFileHandle1);
    }

    @Override
    public Shader getShader(Renderable renderable) {
        try {
            return super.getShader(renderable);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get shader from default shader provider", e);
        }
    }

    @Override
    public Shader createShader(Renderable renderable) {
        GizmoOutlineShader gizmoOutlineShader = new GizmoOutlineShader(new GeomShaderConfig(
                QuantumClient.shader(QuantumClient.id("gizmo_outline.vert")).readString(),
                QuantumClient.shader(QuantumClient.id("gizmo_outline.frag")).readString(),
                QuantumClient.shader(QuantumClient.id("gizmo_outline.geom")).readString()
        ), renderable);

        Shaders.checkShaderCompilation(gizmoOutlineShader.program, "GizmoOutlineShader");
        return gizmoOutlineShader;
    }
}
