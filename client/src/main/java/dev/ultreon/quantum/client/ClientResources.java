package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import dev.ultreon.quantum.util.Identifier;

import static dev.ultreon.quantum.client.QuantumClient.resource;

public class ClientResources {
    public static BitmapFont bitmapFont(Identifier id) {
        return new BitmapFont(resource(id));
    }
}
