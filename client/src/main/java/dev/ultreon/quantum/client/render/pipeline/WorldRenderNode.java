package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.render.ShaderContext;
import dev.ultreon.quantum.client.render.shader.GameShaders;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.entity.Entity;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_NONE;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;

public abstract class WorldRenderNode extends RenderPipeline.RenderNode {
    private Shader shader;

    public void setTexture(Texture texture) {
        if (texture == null) {
            gl.glActiveTexture(GL_NONE);
        } else {
            texture.bind(GL_TEXTURE0);
        }
    }

    protected void render(ModelBatch modelBatch, ShaderProvider shaderProvider, Array<Renderable> input) {
        for (Renderable renderable : input) {
            if (!(shaderProvider instanceof GameShaders gameShaders))
                throw new IllegalStateException("Shader provider is not open");
            ShaderContext.set(gameShaders);
            renderable.environment = this.client.getEnvironment();
            renderable.shader = null;
            this.shader = shaderProvider.getShader(renderable);
            if (this.shader == null) throw new IllegalStateException("Shader not found");
            renderable.shader = this.shader;
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
    public void dumpInfo(PrintStream stream) {
        super.dumpInfo(stream);
        Shader shader = this.shader;
        if (shader != null) {
            stream.println("Shader Hash Code: " + shader.hashCode());
            stream.println("Shader Classname: " + shader.getClass().getName());
            stream.println("Shader Superclass Classname: " + shader.getClass().getSuperclass().getName());
            stream.println("Shader String: " + shader.toString());
        }
    }

    @Override
    public boolean requiresModel() {
        return true;
    }

    private void renderWorldOnce(WorldRenderer worldRenderer, ClientWorld world, Vec3d position, ModelBatch batch) {
        List<Entity> toSort = new ArrayList<>(world.getAllEntities());
        toSort.sort((e1, e2) -> {
            var d1 = e1.getPosition().dst(position);
            var d2 = e2.getPosition().dst(position);
            return Double.compare(d1, d2);
        });
        System.out.println("toSort = " + toSort);
        for (Entity entity : toSort) {
            QuantumClient.PROFILER.section("(Entity #" + entity.getId() + ")", () -> {
                batch.render((output, pool) -> worldRenderer.collectEntity(entity, RenderLayer.WORLD));
            });
        }

        batch.render(RenderLayer.WORLD::finish, worldRenderer.getEnvironment());
    }
}
