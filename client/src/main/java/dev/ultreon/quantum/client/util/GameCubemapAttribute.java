package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;

public class GameCubemapAttribute extends CubemapAttribute {
    private final NamespaceID cubemapId;

    public GameCubemapAttribute(String type, NamespaceID cubemap) {
        super(Attribute.getAttributeType(type), QuantumClient.get().getCubemapManager().get(cubemap));

        this.cubemapId = cubemap;
    }

    public GameCubemapAttribute(long type, NamespaceID cubemap) {
        super(type, QuantumClient.get().getCubemapManager().get(cubemap));

        this.cubemapId = cubemap;
    }

    public NamespaceID getCubemapId() {
        return cubemapId;
    }
}
