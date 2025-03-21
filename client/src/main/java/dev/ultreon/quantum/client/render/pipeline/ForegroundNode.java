package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.input.GameCamera;
import org.checkerframework.common.reflection.qual.NewInstance;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT;
import static com.badlogic.gdx.graphics.GL30.GL_DEPTH_COMPONENT24;
import static dev.ultreon.quantum.client.QuantumClient.LOGGER;

/**
 * The foreground node.
 * <p>
 * This node is responsible for rendering the foreground.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ForegroundNode extends WorldRenderNode {
    /**
     * Renders the foreground.
     *
     * @param textures  The textures.
     * @param camera    The camera.
     * @param deltaTime The delta time.
     */
    @NewInstance
    @Override
    public void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime) {
        Gdx.gl.glClearColor(0F, 0F, 0F, 0F);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glFlush();

        var localPlayer = this.client.player;
        var worldRenderer = this.client.worldRenderer;
        var world = this.client.world;
        if (localPlayer == null || worldRenderer == null || world == null) {
            LOGGER.warn("worldRenderer or localPlayer is null");
            return;
        }

        Texture texture = this.getFrameBuffer().getTextureAttachments().get(0);
        texture.bind(12);
        worldRenderer.renderForeground(client.renderBuffers(), deltaTime);

        textures.put("foreground", texture);
    }

    /**
     * Creates a frame buffer.
     *
     * @return The frame buffer.
     */
    @Override
    protected FrameBuffer createFrameBuffer() {
        return new GLFrameBuffer.FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888)
                .addDepthRenderBuffer(GL_DEPTH_COMPONENT24)
                .build();
    }
}
