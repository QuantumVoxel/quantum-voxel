package dev.ultreon.quantum.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import dev.ultreon.quantum.client.shaders.GeomShaderConfig;
import dev.ultreon.quantum.client.shaders.GizmoShader;
import dev.ultreon.quantum.client.shaders.Shaders;

public class GizmoShaders extends ModelShaders {
    private final GeomShaderConfig config;

    public GizmoShaders(GeomShaderConfig config) {
        super(config);
        this.config = config;
    }

    public GizmoShaders(String vertexShader, String fragmentShader, String geometryShader) {
        this(new GeomShaderConfig(vertexShader, fragmentShader, geometryShader));
    }

    public GizmoShaders(FileHandle vertexShader, FileHandle fragmentShader, FileHandle geometryShader) {
        this(vertexShader.readString(), fragmentShader.readString(), geometryShader.readString());
    }

    @Override
    public Shader createShader(Renderable renderable) {
        GizmoShader gizmoShader = new GizmoShader(renderable, this.config);
        Shaders.checkShaderCompilation(gizmoShader.program, "GizmoShader");
        return gizmoShader;
    }
}
