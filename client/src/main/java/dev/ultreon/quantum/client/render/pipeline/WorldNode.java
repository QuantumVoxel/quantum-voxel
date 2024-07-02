package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Supplier;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.DrawLayer;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.client.shaders.provider.SceneShaders;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.entity.Entity;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.GL30.*;
import static dev.ultreon.quantum.client.QuantumClient.LOGGER;

public class WorldNode extends WorldRenderNode {
    private final Supplier<SceneShaders> shaderProvider = Shaders.SCENE;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input, float deltaTime) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        var localPlayer = this.client.player;
        var worldRenderer = this.client.worldRenderer;
        var world = this.client.world;
        if (localPlayer == null || worldRenderer == null || world == null) {
            LOGGER.warn("worldRenderer or localPlayer is null");
            return input;
        }
        var position = localPlayer.getPosition(client.partialTick);
        List<Entity> toSort = new ArrayList<>(world.getAllEntities());
//        worldRenderer.render(DrawLayer.WORLD, deltaTime);
        toSort.sort((e1, e2) -> {
            var d1 = e1.getPosition().dst(position);
            var d2 = e2.getPosition().dst(position);
            return Double.compare(d1, d2);
        });
        for (Entity entity : toSort) {
            if (entity instanceof LocalPlayer) continue;
            worldRenderer.collectEntity(entity);
        }

        ParticleSystem particleSystem = worldRenderer.getParticleSystem();
        particleSystem.begin();
        particleSystem.updateAndDraw(Gdx.graphics.getDeltaTime());
        particleSystem.end();

        modelBatch.render(particleSystem);

//        DrawLayer.WORLD.finish(input, this.pool());

        worldRenderer.render(deltaTime);

        ValueTracker.setObtainedRenderables(this.pool().getObtained());

        this.render(modelBatch, this.shaderProvider.get(), input);

        textures.put("diffuse", this.getFrameBuffer().getTextureAttachments().get(0));
        textures.put("reflective", this.getFrameBuffer().getTextureAttachments().get(1));
        textures.put("depth", this.getFrameBuffer().getTextureAttachments().get(2));
        textures.put("position", this.getFrameBuffer().getTextureAttachments().get(3));
        textures.put("normal", this.getFrameBuffer().getTextureAttachments().get(4));
        textures.put("specular", this.getFrameBuffer().getTextureAttachments().get(5));
        return input;
    }

    @Override
    protected FrameBuffer createFrameBuffer() {
        return new GLFrameBuffer.FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
                .addColorTextureAttachment(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE)
                .addColorTextureAttachment(GL_RGB8, GL_RGB, GL_UNSIGNED_BYTE)
                .addColorTextureAttachment(GL_RGB8, GL_RGB, GL_UNSIGNED_BYTE)
                .addColorTextureAttachment(GL_RGB8, GL_RGB, GL_UNSIGNED_BYTE)
                .addColorTextureAttachment(GL_RGB8, GL_RGB, GL_UNSIGNED_BYTE)
                .addColorTextureAttachment(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE)
                .addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT16)
                .build();
    }
}
