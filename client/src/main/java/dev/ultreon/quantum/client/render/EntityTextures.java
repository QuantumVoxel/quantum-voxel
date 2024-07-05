package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.LongMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Identifier;
import org.checkerframework.common.returnsreceiver.qual.This;

public class EntityTextures {
    private final LongMap<Texture> textureMap = new LongMap<>();

    public @This EntityTextures set(long attribute, Identifier texture) {
        this.textureMap.put(attribute, QuantumClient.get().getTextureManager().getTexture(texture));
        return this;
    }

    public Texture get(long attribute) {
        return this.textureMap.get(attribute);
    }

    public LongMap<Texture> getTextureMap() {
        return this.textureMap;
    }

    public Material createMaterial() {
        Material material = new Material();
        for (var e : this.textureMap.entries()) {
            material.set(new TextureAttribute(e.key, e.value));
        }
        return material;
    }
}
