package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.ShaderContext;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.shaders.provider.SceneShaders;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The background node.
 * <p>
 * This node is responsible for rendering the background.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class BackgroundNode extends RenderPipeline.RenderNode {
    private final Supplier<SceneShaders> shaderProvider = Shaders.SCENE;

    /**
     * Renders the world.
     *
     * @param batch the batch.
     */
    public void renderWorld(RenderBufferSource batch) {
        @Nullable ClientWorldAccess world = this.client.world;
        @Nullable TerrainRenderer worldRenderer = this.client.worldRenderer;
        LocalPlayer localPlayer = this.client.player;

        if (world != null && worldRenderer != null && this.client.renderWorld && localPlayer != null) {
            worldRenderer.renderBackground(batch, Gdx.graphics.getDeltaTime());
        }
    }

    /**
     * Renders the background.
     *
     * @param textures  the textures.
     * @param camera    the camera.
     * @param deltaTime the delta time.
     */
    @Override
    public void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime) {
        SceneShaders shaderProvider = this.shaderProvider.get();
        ShaderContext.set(shaderProvider);
        this.renderWorld(client.renderBuffers());
        textures.put("skybox", this.getFrameBuffer().getColorBufferTexture());
    }

    /**
     * Whether this node requires a model.
     *
     * @return true if the node requires a model.
     */
    @Override
    public boolean requiresModel() {
        return true;
    }
}
