package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * A custom attribute for game textures.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class GameTextureAttribute extends TextureAttribute {
    private final @NotNull NamespaceID atlasId;
    private final @NotNull NamespaceID name;

    /**
     * Constructs a new GameTextureAttribute with the given type, atlas ID, and region.
     * 
     * @param type The type of the attribute.
     * @param atlasId The ID of the atlas.
     * @param region The region of the texture.
     */
    public GameTextureAttribute(long type, @NotNull NamespaceID atlasId, TextureAtlas.AtlasRegion region) {
        super(type, region);
        this.atlasId = atlasId;

        this.name = NamespaceID.parse(region.name);

        if (region.getTexture() == null)
            throw new IllegalArgumentException("region's texture can't be null");
    }

    /**
     * Constructs a new GameTextureAttribute with the given type, atlas ID, and name.
     * 
     * @param type The type of the attribute.
     * @param atlasId The ID of the atlas.
     * @param name The name of the texture.
     */
    public GameTextureAttribute(String type, @NotNull NamespaceID atlasId, @NotNull NamespaceID name) {
        super(Attribute.getAttributeType(type), QuantumClient.get().getTextureManager().getAtlas(atlasId).findRegion(name.toString()));
        this.atlasId = atlasId;
        this.name = name;
    }

    /**
     * Gets the ID of the atlas.
     * 
     * @return The ID of the atlas.
     */ 
    public @NotNull NamespaceID getAtlasId() {
        return atlasId;
    }

    /**
     * Gets the ID of the texture.
     * 
     * @return The ID of the texture.
     */
    public @NotNull NamespaceID getTextureId() {
        return name;
    }
}
