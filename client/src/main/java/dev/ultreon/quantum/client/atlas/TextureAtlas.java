package dev.ultreon.quantum.client.atlas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.util.NamespaceID;

public class TextureAtlas implements Disposable {
    private final NamespaceID id;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas atlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas emissiveAtlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas normalAtlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas specularAtlas;
    private final com.badlogic.gdx.graphics.g2d.TextureAtlas reflectiveAtlas;
    private final TextureAttribute diffuse;
    private final TextureAttribute emissive;
    private final TextureAttribute normal;
    private final TextureAttribute specular;
    private final TextureAttribute reflective;

    public TextureAtlas(TextureStitcher stitcher, NamespaceID atlasId, PixmapPacker diffusePacker, PixmapPacker emissivePacker, PixmapPacker normalPacker, PixmapPacker specularPacker, PixmapPacker reflectivePacker) {
        this.id = atlasId;
        this.atlas = diffusePacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.emissiveAtlas = emissivePacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.normalAtlas = normalPacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.specularAtlas = specularPacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        this.reflectiveAtlas = reflectivePacker.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);

        diffuse = atlas == null ? null : TextureAttribute.createDiffuse(getTex(this.atlas));
        emissive = emissiveAtlas == null ? null : TextureAttribute.createEmissive(getTex(this.emissiveAtlas));
        normal = normalAtlas == null ? null : TextureAttribute.createNormal(getTex(this.normalAtlas));
        specular = specularAtlas == null ? null : TextureAttribute.createSpecular(getTex(this.specularAtlas));
        reflective = reflectiveAtlas == null ? null : TextureAttribute.createReflection(getTex(this.reflectiveAtlas));

        stitcher.dispose();
    }

    /**
     * @deprecated Use {@link #getDiffuse(NamespaceID)} instead
     */
    @Deprecated(forRemoval = true)
    public TextureRegion get(NamespaceID id) {
        return getDiffuse(id);
    }

    public TextureRegion get(NamespaceID id, TextureAtlasType type) {
        if (id == null) return null;
        return (switch (type) {
            case DIFFUSE -> this.atlas;
            case EMISSIVE -> this.emissiveAtlas;
            case NORMAL -> this.normalAtlas;
            case SPECULAR -> this.specularAtlas;
            case REFLECTIVE -> this.reflectiveAtlas;
        }).findRegion(id.toString());
    }

    public TextureRegion getEmissive(NamespaceID id) {
        if (id == null) return null;
        return this.emissiveAtlas.findRegion(id.toString());
    }

    public TextureRegion getNormal(NamespaceID id) {
        if (id == null) return null;
        return this.normalAtlas.findRegion(id.toString());
    }

    public TextureRegion getSpecular(NamespaceID id) {
        if (id == null) return null;
        return this.specularAtlas.findRegion(id.toString());
    }

    public TextureRegion getReflective(NamespaceID id) {
        if (id == null) return null;
        return this.reflectiveAtlas.findRegion(id.toString());
    }

    public TextureRegion getDiffuse(NamespaceID id) {
        if (id == null) return null;
        return this.atlas.findRegion(id.toString());
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

    public void apply(Material material, TextureAtlasType type) {
        material.set(switch (type) {
            case DIFFUSE -> diffuse;
            case EMISSIVE -> emissive;
            case NORMAL -> normal;
            case SPECULAR -> specular;
            case REFLECTIVE -> reflective;
        });
    }

    public NamespaceID getId() {
        return id;
    }

    public enum TextureAtlasType {
        DIFFUSE,
        EMISSIVE,
        NORMAL,
        SPECULAR,
        REFLECTIVE
    }
}
