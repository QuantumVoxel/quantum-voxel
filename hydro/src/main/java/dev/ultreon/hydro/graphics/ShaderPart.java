package dev.ultreon.hydro.graphics;

public class ShaderPart {
    public final String source;
    public final ShaderType type;

    public ShaderPart(String source, ShaderType type) {
        this.source = source;
        this.type = type;
    }
}
