package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class GameTextureAttribute extends TextureAttribute {
    private final @NotNull NamespaceID atlasId;
    private final @NotNull NamespaceID name;

    public GameTextureAttribute(long type, @NotNull NamespaceID atlasId, TextureAtlas.AtlasRegion region) {
        super(type, region);
        this.atlasId = atlasId;

        this.name = NamespaceID.parse(region.name);

        if (region.getTexture() == null)
            throw new IllegalArgumentException("region's texture can't be null");
    }

    public GameTextureAttribute(String type, @NotNull NamespaceID atlasId, @NotNull NamespaceID name) {
        super(Attribute.getAttributeType(type), QuantumClient.get().getTextureManager().getAtlas(atlasId).findRegion(name.toString()));
        this.atlasId = atlasId;
        this.name = name;
    }

    public @NotNull NamespaceID getAtlasId() {
        return atlasId;
    }

    public @NotNull NamespaceID getTextureId() {
        return name;
    }
}
