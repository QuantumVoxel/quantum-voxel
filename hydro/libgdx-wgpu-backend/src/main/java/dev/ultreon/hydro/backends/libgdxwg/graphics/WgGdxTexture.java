package dev.ultreon.hydro.backends.libgdxwg.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.github.xpenatan.webgpu.WGPUTextureFormat;
import com.github.xpenatan.webgpu.WGPUTextureUsage;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import dev.ultreon.hydro.backends.libgdxwg.fs.WgGdxPathHandle;
import dev.ultreon.hydro.graphics.Texture;

public class WgGdxTexture extends WgTexture implements Texture {
    public WgGdxTexture(String label, int width, int height, boolean useMipMaps, boolean renderAttachment, WGPUTextureFormat format, int numSamples) {
        super(label, width, height, useMipMaps, renderAttachment, format, numSamples);
    }

    public WgGdxTexture(String label, int width, int height, boolean useMipMaps, WGPUTextureUsage textureUsage, WGPUTextureFormat format, int numSamples) {
        super(label, width, height, useMipMaps, textureUsage, format, numSamples);
    }

    public WgGdxTexture(String label, int width, int height, boolean useMipMaps, WGPUTextureUsage textureUsage, WGPUTextureFormat format, int numSamples, WGPUTextureFormat viewFormat) {
        super(label, width, height, useMipMaps, textureUsage, format, numSamples, viewFormat);
    }

    public WgGdxTexture(String label, int width, int height, int numLayers, boolean useMipMaps, WGPUTextureUsage textureUsage) {
        super(label, width, height, numLayers, useMipMaps, textureUsage);
    }

    public WgGdxTexture(String fileName) {
        super(fileName);
    }

    public WgGdxTexture(String fileName, boolean useMipMaps) {
        super(fileName, useMipMaps);
    }

    public WgGdxTexture(String fileName, boolean useMipMaps, boolean isColor) {
        super(fileName, useMipMaps, isColor);
    }

    public WgGdxTexture(WgGdxPathHandle file) {
        super(file.toGdx());
    }

    public WgGdxTexture(WgGdxPathHandle file, boolean useMipMaps) {
        super(file.toGdx(), useMipMaps);
    }

    public WgGdxTexture(WgGdxPathHandle file, boolean useMipMaps, boolean isColor) {
        super(file.toGdx(), useMipMaps, isColor);
    }

    public WgGdxTexture(WgGdxPathHandle file, Pixmap.Format format, boolean useMipMaps) {
        super(file.toGdx(), format, useMipMaps);
    }

    public WgGdxTexture(WgGdxPathHandle file, Pixmap.Format format, boolean useMipMaps, boolean isColor) {
        super(file.toGdx(), format, useMipMaps, isColor);
    }

    public WgGdxTexture(Pixmap pixmap) {
        super(pixmap);
    }

    public WgGdxTexture(Pixmap pixmap, String label) {
        super(pixmap, label);
    }

    public WgGdxTexture(Pixmap pixmap, String label, boolean isColor) {
        super(pixmap, label, isColor);
    }

    public WgGdxTexture(TextureData data) {
        super(data);
    }

    public WgGdxTexture(TextureData data, String label) {
        super(data, label);
    }

    public WgGdxTexture(TextureData data, String label, boolean isColor) {
        super(data, label, isColor);
    }

    @Override
    public void destroy() {
        this.dispose();
    }
}
