package dev.ultreon.quantum.client.atlas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.util.TextureOffset;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.util.Identifier;

import java.util.Map;

public class TextureAtlas implements Disposable {
    private final TextureStitcher stitcher;
    private final Identifier id;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas atlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas emissiveAtlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas normalAtlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas specularAtlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas reflectiveAtlas;

    public TextureAtlas(TextureStitcher stitcher, Identifier atlasId, PixmapPacker diffusePacker, PixmapPacker emissivePacker, PixmapPacker normalPacker, PixmapPacker specularPacker, PixmapPacker reflectivePacker) {
        this.stitcher = stitcher;
        this.id = atlasId;
        this.atlas = diffusePacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.emissiveAtlas = emissivePacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.normalAtlas = normalPacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.specularAtlas = specularPacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.reflectiveAtlas = reflectivePacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);

        this.stitcher.dispose();
    }

    public TextureRegion get(Identifier id) {
        return get(id, TextureAtlasType.DIFFUSE);
    }

    public TextureRegion get(Identifier id, TextureAtlasType type) {
        if (id == null) return null;
        TextureRegion textureRegion = (switch (type) {
            case DIFFUSE -> this.atlas;
            case EMISSIVE -> this.emissiveAtlas;
            case NORMAL -> this.normalAtlas;
            case SPECULAR -> this.specularAtlas;
            case REFLECTIVE -> this.reflectiveAtlas;
        }).findRegion(id.toString());
        if (textureRegion == null) return null;
        textureRegion.flip(false, true);
        return textureRegion;
    }

    public TextureRegion getEmissive(Identifier id) {
        if (id == null) return null;
        TextureRegion textureRegion = this.emissiveAtlas.findRegion(id.toString());
        textureRegion.flip(false, true);
        return textureRegion;
    }

    public Texture getTexture() {
        return getTex(this.atlas);
    }

    public Texture getEmissiveTexture() {
        return getTex(this.emissiveAtlas);
    }

    public Texture getNormalTexture() {
        return getTex(this.normalAtlas);
    }

    public Texture getSpecularTexture() {
        return getTex(this.specularAtlas);
    }

    public Texture getReflectivnessTexture() {
        return getTex(this.reflectiveAtlas);
    }

    private Texture getTex(com.badlogic.gdx.graphics.g2d.TextureAtlas atlas) {
        if (atlas.getTextures().isEmpty()) return null;
        return atlas.getTextures().first();
    }

    @Override
    public void dispose() {
        this.atlas.dispose();
        this.emissiveAtlas.dispose();
    }

    public enum TextureAtlasType {
        DIFFUSE,
        EMISSIVE,
        NORMAL,
        SPECULAR,
        REFLECTIVE
    }
}
