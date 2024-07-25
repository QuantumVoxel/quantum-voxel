package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.management.TextureAtlasManager;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TextureAtlasAttribute extends TextureAttribute {
    private final @NotNull Identifier atlasId;

    public TextureAtlasAttribute(long type, TextureAtlas atlas) {
        super(type, atlas.getTextures().first());

        this.atlasId = QuantumClient.get().getTextureManager().getAtlasId(atlas);
    }

    public TextureAtlasAttribute(String type, @NotNull Identifier atlasId) {
        super(Attribute.getAttributeType(type), QuantumClient.get().getTextureManager().getAtlas(atlasId).getTextures().first());
        this.atlasId = atlasId;
    }

    public @NotNull Identifier getAtlasId() {
        return atlasId;
    }
}
