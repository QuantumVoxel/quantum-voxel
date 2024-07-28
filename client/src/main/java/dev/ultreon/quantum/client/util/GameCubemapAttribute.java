package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Identifier;

public class GameCubemapAttribute extends CubemapAttribute {
    private final Identifier cubemapId;

    public GameCubemapAttribute(String type, Identifier cubemap) {
        super(Attribute.getAttributeType(type), QuantumClient.get().getCubemapManager().get(cubemap));

        this.cubemapId = cubemap;
    }

    public GameCubemapAttribute(long type, Identifier cubemap) {
        super(type, QuantumClient.get().getCubemapManager().get(cubemap));

        this.cubemapId = cubemap;
    }

    public Identifier getCubemapId() {
        return cubemapId;
    }
}
