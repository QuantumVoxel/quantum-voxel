package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import dev.ultreon.quantum.util.NamespaceID;

import static dev.ultreon.quantum.client.QuantumClient.resource;

public class ClientResources {
    public static BitmapFont bitmapFont(NamespaceID id) {
        return new BitmapFont(resource(id));
    }
}
