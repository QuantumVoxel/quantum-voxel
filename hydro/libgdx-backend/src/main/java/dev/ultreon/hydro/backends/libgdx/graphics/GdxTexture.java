package dev.ultreon.hydro.backends.libgdx.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import dev.ultreon.hydro.backends.libgdx.fs.GdxPathHandle;

public class GdxTexture extends Texture implements dev.ultreon.hydro.graphics.Texture {
    public GdxTexture(String internalPath) {
        super(internalPath);
    }

    public GdxTexture(GdxPathHandle file) {
        super(file.toGdx());
    }

    public GdxTexture(GdxPathHandle file, boolean useMipMaps) {
        super(file.toGdx(), useMipMaps);
    }

    public GdxTexture(GdxPathHandle file, Pixmap.Format format, boolean useMipMaps) {
        super(file.toGdx(), format, useMipMaps);
    }

    public GdxTexture(Pixmap pixmap) {
        super(pixmap);
    }

    public GdxTexture(Pixmap pixmap, boolean useMipMaps) {
        super(pixmap, useMipMaps);
    }

    public GdxTexture(Pixmap pixmap, Pixmap.Format format, boolean useMipMaps) {
        super(pixmap, format, useMipMaps);
    }

    public GdxTexture(int width, int height, Pixmap.Format format) {
        super(width, height, format);
    }

    public GdxTexture(TextureData data) {
        super(data);
    }

    @Override
    public void destroy() {
        this.dispose();
    }
}
