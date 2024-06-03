package dev.ultreon.quantum.client.texture;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import dev.ultreon.quantum.util.Identifier;

public class TexturePacker {
    private final Identifier id;
    private final PixmapPacker stitcher;

    public TexturePacker(Identifier id, int maxSize) {
        this.id = id;
        this.stitcher = new PixmapPacker(maxSize, maxSize, Pixmap.Format.RGBA4444, 0, false, new PixmapPacker.GuillotineStrategy());
    }

    public TextureAtlas stitch() {
        return this.stitcher.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, true);
    }
}
