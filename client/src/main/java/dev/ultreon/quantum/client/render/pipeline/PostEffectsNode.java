package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.util.GameCamera;
import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * The post effects node.
 * <p>
 * This node is responsible for rendering the post effects.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@Experimental
public class PostEffectsNode extends RenderPipeline.RenderNode {
    @Override
    public void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime) {
        // Implement post-effects rendering logic here
        System.out.println("Post Effects Rendering");
    }
}
