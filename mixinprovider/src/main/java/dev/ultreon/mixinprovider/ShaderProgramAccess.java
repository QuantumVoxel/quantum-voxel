package dev.ultreon.mixinprovider;

public interface ShaderProgramAccess {
    void quantum$compileShaders(String vertexShader, String fragmentShader, String geometryShader);
}
