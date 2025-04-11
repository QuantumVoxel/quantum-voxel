package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.entity.Entity;
import org.checkerframework.common.reflection.qual.NewInstance;

import static com.badlogic.gdx.graphics.GL30.GL_DEPTH_COMPONENT24;
import static dev.ultreon.quantum.client.QuantumClient.LOGGER;

/**
 * The world node.
 * <p>
 * This node is responsible for rendering the world.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class WorldNode extends WorldRenderNode {

    @NewInstance
    @Override
    public void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        var localPlayer = this.client.player;
        var worldRenderer = this.client.worldRenderer;
        var world = this.client.world;
        if (localPlayer == null || worldRenderer == null || world == null) {
            LOGGER.warn("worldRenderer or localPlayer is null");
            return;
        }
        var position = localPlayer.getPosition(client.partialTick);
        Array<Entity> toSort = new Array<>(world.getAllEntities());
        worldRenderer.render(client.renderBuffers(), deltaTime);
        toSort.sort((e1, e2) -> {
            var d1 = e1.getPosition().dst(position);
            var d2 = e2.getPosition().dst(position);
            return Double.compare(d1, d2);
        });
        for (Entity entity : toSort.toArray(Entity.class)) {
            if (entity instanceof LocalPlayer) continue;
            worldRenderer.collectEntity(entity, client.renderBuffers());
        }

        ParticleSystem particleSystem = worldRenderer.getParticleSystem();
        particleSystem.begin();
        particleSystem.updateAndDraw(Gdx.graphics.getDeltaTime());
        particleSystem.end();
//            modelBatch.render(particleSystem);
        // TODO add particle system

        ValueTracker.setObtainedRenderables(this.pool().getObtained());

        textures.put("diffuse", this.getFrameBuffer().getTextureAttachments().get(0));
        textures.put("reflective", this.getFrameBuffer().getTextureAttachments().get(1));
        textures.put("depth", this.getFrameBuffer().getTextureAttachments().get(2));
        textures.put("position", this.getFrameBuffer().getTextureAttachments().get(3));
        textures.put("normal", this.getFrameBuffer().getTextureAttachments().get(4));
        textures.put("specular", this.getFrameBuffer().getTextureAttachments().get(5));
    }

    @Override
    protected FrameBuffer createFrameBuffer() {
        return new GLFrameBuffer.FrameBufferBuilder(QuantumClient.get().getWidth(), QuantumClient.get().getHeight())
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888)
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888)
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888)
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888)
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888)
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888)
                .addDepthRenderBuffer(GL_DEPTH_COMPONENT24)
                .build();
    }
}
