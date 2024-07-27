package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Supplier;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.render.ShaderContext;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.client.shaders.provider.SceneShaders;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import org.jetbrains.annotations.Nullable;

public class BackgroundNode extends RenderPipeline.RenderNode {
    private final Supplier<SceneShaders> shaderProvider = Shaders.SCENE;

    public void renderWorld(ModelBatch batch) {
        @Nullable ClientWorldAccess world = this.client.world;
        @Nullable TerrainRenderer worldRenderer = this.client.worldRenderer;
        LocalPlayer localPlayer = this.client.player;

        if (world != null && worldRenderer != null && this.client.renderWorld && localPlayer != null) {
            this.renderWorldOnce(worldRenderer, world, localPlayer.getPosition(client.partialTick), batch);
        }
    }

    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input, float deltaTime) {
        SceneShaders shaderProvider = this.shaderProvider.get();
        ShaderContext.set(shaderProvider);
        this.renderWorld(modelBatch);
        textures.put("skybox", this.getFrameBuffer().getColorBufferTexture());
        return input;
    }

    @Override
    public boolean requiresModel() {
        return true;
    }

    private void renderWorldOnce(@Nullable TerrainRenderer worldRenderer, @Nullable ClientWorldAccess world, Vec3d position, ModelBatch batch) {
        if (worldRenderer != null) {
            worldRenderer.updateBackground();
        }
        batch.render(RenderLayer.BACKGROUND::finish);
    }
}
