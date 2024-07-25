package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class GameTextureAttribute extends TextureAttribute {
    private final @NotNull Identifier atlasId;
    private final @NotNull Identifier name;

    public GameTextureAttribute(long type, @NotNull Identifier atlasId, TextureAtlas.AtlasRegion region) {
        super(type, region);
        this.atlasId = atlasId;

        this.name = Identifier.parse(region.name);

        if (region.getTexture() == null)
            throw new IllegalArgumentException("region's texture can't be null");
    }

    public GameTextureAttribute(String type, @NotNull Identifier atlasId, @NotNull Identifier name) {
        super(Attribute.getAttributeType(type), QuantumClient.get().getTextureManager().getAtlas(atlasId).findRegion(name.toString()));
        this.atlasId = atlasId;
        this.name = name;
    }

    public @NotNull Identifier getAtlasId() {
        return atlasId;
    }

    public @NotNull Identifier getTextureId() {
        return name;
    }
}
