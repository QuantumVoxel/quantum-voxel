package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Supplier;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.Scene3D;
import dev.ultreon.quantum.client.render.ShaderContext;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.client.shaders.provider.WorldShaderProvider;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.WorldRenderer;

public class BackgroundNode extends RenderPipeline.RenderNode {
    private final Supplier<WorldShaderProvider> shaderProvider = Shaders.WORLD;

    protected void render(ModelBatch modelBatch, ShaderProvider shaderProvider, Array<Renderable> input) {
        for (Renderable renderable : input) {
            renderable.environment = this.client.getEnvironment();
            renderable.shader = null;
            Shader shader = shaderProvider.getShader(renderable);
            if (shader == null) throw new IllegalStateException("Shader not found");
            renderable.shader = shader;
            modelBatch.render(renderable);
        }
    }

    public void renderWorld(ModelBatch batch) {
        ClientWorld world = this.client.world;
        WorldRenderer worldRenderer = this.client.worldRenderer;
        LocalPlayer localPlayer = this.client.player;

        if (world != null && worldRenderer != null && this.client.renderWorld && localPlayer != null) {
            this.renderWorldOnce(worldRenderer, world, localPlayer.getPosition(client.partialTick), batch);
        }
    }

    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        WorldShaderProvider shaderProvider = this.shaderProvider.get();
        ShaderContext.set(shaderProvider);
        this.renderWorld(modelBatch);
        textures.put("skybox", this.getFrameBuffer().getColorBufferTexture());
        return input;
    }

    @Override
    public boolean requiresModel() {
        return true;
    }

    private void renderWorldOnce(WorldRenderer worldRenderer, ClientWorld world, Vec3d position, ModelBatch batch) {
        worldRenderer.updateBackground();
        batch.render(Scene3D.BACKGROUND::finish);
    }
}
