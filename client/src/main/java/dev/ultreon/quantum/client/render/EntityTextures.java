package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.LongMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;
import org.checkerframework.common.returnsreceiver.qual.This;

/**
 * The EntityTextures class manages a mapping between entity attributes and their associated textures.
 * It allows setting and retrieving textures for given attributes, as well as creating materials based on the defined textures.
 */
public class EntityTextures {
    private final LongMap<Texture> textureMap = new LongMap<>();

    /**
     * Sets the texture for a given attribute and returns this object for method chaining.
     *
     * @param attribute the attribute key to identify the texture.
     * @param texture the NamespaceID of the texture to set.
     * @return the current instance of EntityTextures.
     */
    public @This EntityTextures set(long attribute, NamespaceID texture) {
        this.textureMap.put(attribute, QuantumClient.get().getTextureManager().getTexture(texture));
        return this;
    }

    /**
     * Retrieves the texture associated with the given attribute.
     *
     * @param attribute the attribute key for which the texture is to be retrieved.
     * @return the Texture associated with the specified attribute.
     */
    public Texture get(long attribute) {
        return this.textureMap.get(attribute);
    }

    /**
     * Retrieves the mapping between entity attributes and their associated textures.
     *
     * @return a LongMap containing the textures mapped to their corresponding attributes.
     */
    public LongMap<Texture> getTextureMap() {
        return this.textureMap;
    }

    /**
     * Creates a new Material object based on the textures mapped to entity attributes.
     * Iterates over the texture map and sets corresponding TextureAttributes to the material.
     *
     * @return a new Material object with the mapped textures applied as attributes.
     */
    public Material createMaterial() {
        Material material = new Material();
        for (var e : this.textureMap.entries()) {
            material.set(new TextureAttribute(e.key, e.value));
        }
        return material;
    }
}
