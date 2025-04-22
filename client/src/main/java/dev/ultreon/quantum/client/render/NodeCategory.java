package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.*;
import org.jetbrains.annotations.ApiStatus;

/**
 * The "node category" class represents a category of scenes in the game.
 * It extends the GameObject class and implements the RenderableProvider interface.
 * <p>
 * This class is used to manage the scenes in the game.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class NodeCategory extends GameObject implements RenderableProvider {
    /**
     * Constructs a new "node category" object.
     */
    @ApiStatus.Internal
    public NodeCategory() {

    }

    /**
     * Gets the background node category.
     * 
     * @return The background node category.
     */
    public static NodeCategory getBackground() {
        return QuantumClient.get().backgroundCat;
    }

    /**
     * Gets the world node category.
     * 
     * @return The world node category.
     */
    public static NodeCategory getWorld() {
        return QuantumClient.get().worldCat;
    }

    /**
     * Gets the main node category.
     * 
     * @return The main node category.
     */
    public static NodeCategory getMain() {
        return QuantumClient.get().mainCat;
    }

    /**
     * Updates the node category.
     * 
     * @param delta The delta time.
     */
    public void update(float delta) {
        for (GameNode child : getChildren()) {
            if (child instanceof GameObject) {
                GameObject gameObject = (GameObject) child;
                gameObject.combined.set(gameObject.transform)
                        .translate(gameObject.translation)
                        .rotate(Vector3.X, gameObject.rotation.x)
                        .rotate(Vector3.Y, gameObject.rotation.y)
                        .rotate(Vector3.Z, gameObject.rotation.z)
                        .scale(gameObject.scale.x, gameObject.scale.y, gameObject.scale.z);
            }

            child.update(delta);
        }
    }
}
