package dev.ultreon.quantum.client.shaders;

import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

public class GeomShaderConfig extends DefaultShader.Config {
    public final String geometryShader;

    public GeomShaderConfig(final String vertexShader, final String fragmentShader, final String geometryShader) {
        super(vertexShader, fragmentShader);
        this.geometryShader = geometryShader;
    }

    public GeomShaderConfig() {
        this(DefaultShader.getDefaultVertexShader(), DefaultShader.getDefaultFragmentShader(), WorldShader.getDefaultGeometryShader());
    }
}
