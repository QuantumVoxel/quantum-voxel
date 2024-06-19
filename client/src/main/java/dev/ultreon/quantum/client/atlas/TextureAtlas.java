package dev.ultreon.quantum.client.atlas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.client.util.TextureOffset;
import dev.ultreon.quantum.util.Identifier;

import java.util.Map;

public class TextureAtlas implements Disposable {
    private final TextureStitcher stitcher;
    private final Identifier id;
    private final Texture atlas;
    private final Texture emissiveAtlas;
    private final Texture normalAtlas;
    private final Texture specularAtlas;
    private final Texture reflectiveAtlas;
    private final Map<Identifier, TextureOffset> uvMap;

    public TextureAtlas(TextureStitcher stitcher, Identifier id, Texture atlas, Texture emissiveAtlas, Texture normalAtlas, Texture specularAtlas, Texture reflectiveAtlas, Map<Identifier, TextureOffset> uvMap) {
        this.stitcher = stitcher;
        this.id = id;
        this.atlas = atlas;
        this.emissiveAtlas = emissiveAtlas;
        this.normalAtlas = normalAtlas;
        this.specularAtlas = specularAtlas;
        this.reflectiveAtlas = reflectiveAtlas;
        this.uvMap = uvMap;

        QuantumClient client = QuantumClient.get();
        TextureManager textureManager = client.getTextureManager();
        textureManager.registerTexture(this.id.mapPath(path -> "atlas/" + path + ".atlas"), atlas);
        textureManager.registerTexture(this.id.mapPath(path -> "atlas/" + path + ".emissive.atlas"), emissiveAtlas);
        textureManager.registerTexture(this.id.mapPath(path -> "atlas/" + path + ".normal.atlas"), emissiveAtlas);
        textureManager.registerTexture(this.id.mapPath(path -> "atlas/" + path + ".specular.atlas"), emissiveAtlas);
        textureManager.registerTexture(this.id.mapPath(path -> "atlas/" + path + ".reflective.atlas"), emissiveAtlas);
    }

    public TextureRegion get(Identifier id) {
        if (id == null) return null;
        TextureOffset textureOffset = this.uvMap.get(id);
        if (textureOffset == null) return null;
        TextureRegion textureRegion = new TextureRegion(this.atlas, textureOffset.u(), textureOffset.v(), textureOffset.uWidth(), textureOffset.vHeight());
        textureRegion.flip(false, true);
        return textureRegion;
    }

    public TextureRegion getEmissive(Identifier id) {
        if (id == null) return null;
        TextureOffset textureOffset = this.uvMap.get(id);
        if (textureOffset == null) return null;
        TextureRegion textureRegion = new TextureRegion(this.emissiveAtlas, textureOffset.u(), textureOffset.v(), textureOffset.uWidth(), textureOffset.vHeight());
        textureRegion.flip(false, true);
        return textureRegion;
    }

    public Texture getTexture() {
        return this.atlas;
    }

    public Texture getEmissiveTexture() {
        return this.emissiveAtlas;
    }

    public Texture getNormalTexture() {
        return this.normalAtlas;
    }

    public Texture getSpecularTexture() {
        return this.specularAtlas;
    }

    public Texture getReflectivnessTexture() {
        return this.reflectiveAtlas;
    }

    @Override
    public void dispose() {
        this.stitcher.dispose();
        this.atlas.dispose();
        this.emissiveAtlas.dispose();
    }
}
