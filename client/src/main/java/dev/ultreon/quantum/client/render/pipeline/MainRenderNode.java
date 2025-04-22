package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.client.render.pipeline.RenderPipeline.RenderNode;
import dev.ultreon.quantum.util.NamespaceID;

import java.io.PrintStream;

import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.*;

/**
 * The main render node.
 * <p>
 * This node is responsible for rendering the main scene.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class MainRenderNode extends RenderNode {
    private Mesh quad = createFullScreenQuad();
    private float blurScale = 0f;
    private Texture vignetteTex;

    /**
     * Renders the scene with the given textures and parameters.
     *
     * @param textures  The map containing textures.
     * @param camera    The GameCamera representing the camera.
     * @param deltaTime The time passed since the last frame.
     */
    @Override
    public void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime) {
        // Extract textures
        if (vignetteTex == null) {
            vignetteTex = client.getTextureManager().getTexture(new NamespaceID("textures/gui/vignette.png"));
        }

        // Handle blur effect
        if (blurScale > 0f) {
            client.renderer.blurred(
                    blurScale,
                    ClientConfig.blurRadius * blurScale,
                    true,
                    1,
                    () -> render(textures)
            );
        } else {
            render(textures);
        }

        // Disable blending and end rendering
        client.renderer.getBatch().disableBlending();
        client.renderer.end();
        client.spriteBatch.setShader(null);

        gl.glActiveTexture(GL_TEXTURE0);

        // Enable blending and set blend function
        client.renderer.getBatch().enableBlending();
    }

    private void render(ObjectMap<String, Texture> textures) {
        Texture texture = textures.get("skybox");
        if (texture != null) {
            client.spriteBatch.begin();
            client.spriteBatch.draw(texture, 0, 0, QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
            client.spriteBatch.end();
        }

        // End modelBatch and begin rendering
        client.renderBuffers().end();
        client.renderer.begin();
        client.renderer.getBatch().enableBlending();
        client.renderer.getBatch().setBlendFunctionSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Resizes the quad.
     *
     * @param width The width.
     * @param height The height.
     */
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        quad.dispose();
        quad = createFullScreenQuad();
    }

    /**
     * Dumps the info.
     *
     * @param stream The stream.
     */
    @Override
    public void dumpInfo(PrintStream stream) {
        super.dumpInfo(stream);
    }

    /**
     * Creates a full screen quad.
     *
     * @return The full screen quad.
     */
    public Mesh createFullScreenQuad() {
        float[] vertices = new float[20];
        int i = 0;

        vertices[i++] = -1;
        vertices[i++] = -1;
        vertices[i++] = 0;
        vertices[i++] = 0f;
        vertices[i++] = 0f;

        vertices[i++] = 1f;
        vertices[i++] = -1;
        vertices[i++] = 0;
        vertices[i++] = 1f;
        vertices[i++] = 0f;

        vertices[i++] = 1f;
        vertices[i++] = 1f;
        vertices[i++] = 0;
        vertices[i++] = 1f;
        vertices[i++] = 1f;

        vertices[i++] = -1;
        vertices[i++] = 1f;
        vertices[i++] = 0;
        vertices[i++] = 0f;
        vertices[i] = 1f;

        Mesh mesh = new Mesh(true, 4, 0,
                VertexAttribute.Position(),
                VertexAttribute.TexCoords(0)
        );

        mesh.setVertices(vertices);
        return mesh;
    }

    /**
     * Blurs the scene.
     *
     * @param blurScale The blur scale.
     */
    public void blur(float blurScale) {
        blurScale = blurScale;
    }
}
