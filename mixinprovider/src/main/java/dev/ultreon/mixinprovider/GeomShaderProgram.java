package dev.ultreon.mixinprovider;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GeomShaderProgram extends ShaderProgram {
    public GeomShaderProgram(String vertexShader, String fragmentShader, String geometryShader) {
        super(vertexShader, fragmentShader + "\0" + geometryShader);
    }
}
