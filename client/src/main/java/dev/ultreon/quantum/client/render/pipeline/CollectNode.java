package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.util.GameCamera;
import org.checkerframework.common.reflection.qual.NewInstance;

/**
 * The collect node.
 * <p>
 * This node is responsible for collecting the renderables.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class CollectNode extends RenderPipeline.RenderNode {
    /**
     * Renders the collect node.
     *
     * @param textures  The textures.
     * @param camera    The camera.
     * @param deltaTime The delta time.
     */
    @NewInstance
    @Override
    public void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime) {
    }
}
