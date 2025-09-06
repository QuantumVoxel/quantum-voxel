package dev.ultreon.quantum.client.render.context;

public interface ColorSource extends UniformSetter, UniformFactory {
    boolean isTexture();
    boolean isColor();
}
