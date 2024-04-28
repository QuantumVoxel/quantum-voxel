package com.ultreon.quantum.client.atlas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.texture.TextureManager;
import com.ultreon.quantum.client.util.TextureOffset;
import com.ultreon.quantum.util.Identifier;

import java.util.Map;

public class TextureAtlas implements Disposable {
    private final TextureStitcher stitcher;
    private final Identifier id;
    private final Texture atlas;
    private final Texture emissiveAtlas;
    private final Map<Identifier, TextureOffset> uvMap;

    public TextureAtlas(TextureStitcher stitcher, Identifier id, Texture atlas, Texture emissiveAtlas, Map<Identifier, TextureOffset> uvMap) {
        this.stitcher = stitcher;
        this.id = id;
        this.atlas = atlas;
        this.emissiveAtlas = emissiveAtlas;
        this.uvMap = uvMap;

        QuantumClient client = QuantumClient.get();
        TextureManager textureManager = client.getTextureManager();
        textureManager.registerTexture(this.id.mapPath(path -> "atlas/" + path + ".png-atlas"), atlas);
        textureManager.registerTexture(this.id.mapPath(path -> "atlas/" + path + ".emissive.png-atlas"), emissiveAtlas);
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

    @Override
    public void dispose() {
        this.stitcher.dispose();
        this.atlas.dispose();
        this.emissiveAtlas.dispose();
    }
}
