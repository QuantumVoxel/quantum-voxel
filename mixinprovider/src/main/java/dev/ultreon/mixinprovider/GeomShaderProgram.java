package dev.ultreon.mixinprovider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class GeomShaderProgram extends ShaderProgram {
    public GeomShaderProgram(FileHandle vertexShader, FileHandle fragmentShader, FileHandle geometryShader) {
        this(vertexShader.readString(), fragmentShader.readString(), geometryShader.readString());
    }
    public GeomShaderProgram(String vertexShader, String fragmentShader, String geometryShader) {
        super(vertexShader, fragmentShader + "\0" + geometryShader);
    }
}
