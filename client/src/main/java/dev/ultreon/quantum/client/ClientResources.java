package dev.ultreon.quantum.client;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import dev.ultreon.quantum.util.NamespaceID;

import static dev.ultreon.quantum.client.QuantumClient.resource;

/**
 * A class that contains methods for loading resources.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ClientResources {
    /**
     * Loads a bitmap font from the resource.
     *
     * @param id The namespace ID of the font.
     * @return The loaded bitmap font.
     */
    public static BitmapFont bitmapFont(NamespaceID id) {
        return new BitmapFont(resource(id));
    }
}
