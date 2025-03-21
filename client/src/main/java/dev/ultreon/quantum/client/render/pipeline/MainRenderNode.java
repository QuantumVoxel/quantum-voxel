package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.render.ShaderPrograms;
import dev.ultreon.quantum.client.render.pipeline.RenderPipeline.RenderNode;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.util.NamespaceID;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.function.Supplier;

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
    private final Supplier<ShaderProgram> program = ShaderPrograms.MAIN;
    private float blurScale = 0f;
    private Texture vignetteTex;

    /**
     * Renders the scene with the given textures and parameters.
     *
     * @param textures  The map containing textures.
     * @param camera    The GameCamera representing the camera.
     * @param deltaTime The time passed since the last frame.
     */
    @NewInstance
    @Override
    public void render(ObjectMap<String, Texture> textures, GameCamera camera, float deltaTime) {
        // Extract textures
        if (vignetteTex == null) {
            vignetteTex = client.getTextureManager().getTexture(new NamespaceID("textures/gui/vignette.png"));
        }

        Texture depthTex = textures.get("depth");
        Texture skyboxTex = textures.get("skybox");
        Texture diffuseTex = textures.get("diffuse");
        Texture positionTex = textures.get("position");
        Texture normalTex = textures.get("normal");
        Texture reflectiveTex = textures.get("reflective");
        Texture specularTex = textures.get("specular");
        Texture foregroundTex = textures.get("foreground");

        // End modelBatch and begin rendering
        client.renderBuffers().end();
        client.renderer.begin();
        client.renderer.getBatch().enableBlending();
        client.renderer.getBatch().setBlendFunctionSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Handle blur effect
        if (blurScale > 0f) {
            client.renderer.blurred(blurScale, ClientConfig.blurRadius * blurScale, true, 1, () -> {
                render(skyboxTex, diffuseTex, normalTex, reflectiveTex, depthTex, positionTex, specularTex, foregroundTex);
            });
        } else {
            render(skyboxTex, diffuseTex, normalTex, reflectiveTex, depthTex, positionTex, specularTex, foregroundTex);
        }

        // Disable blending and end rendering
        client.renderer.getBatch().disableBlending();
        client.renderer.end();
        client.spriteBatch.setShader(null);

        gl.glActiveTexture(GL_TEXTURE0);

        // Show render pipeline
        if (GamePlatform.get().showRenderPipeline()) {
            client.renderer.begin();
            client.renderer.getBatch().enableBlending();
            client.spriteBatch.draw(diffuseTex, (float) 0, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            client.spriteBatch.flush();
            client.spriteBatch.draw(positionTex, (float) (Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            client.spriteBatch.flush();
            client.spriteBatch.draw(normalTex, (float) (2 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            client.spriteBatch.flush();
            client.spriteBatch.draw(foregroundTex, (float) (3 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            client.spriteBatch.flush();
            client.spriteBatch.draw(skyboxTex, (float) (4 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            client.renderer.end();
        }

        // Enable blending and set blend function
        client.renderer.getBatch().enableBlending();
    }

    /**
     * Renders the scene with the given textures and parameters.
     *
     * @param skyboxTex The skybox texture.
     * @param diffuseTex The diffuse texture.
     * @param normalTex The normal texture.
     * @param reflectiveTex The reflective texture.
     * @param depthTex The depth texture.
     * @param positionTex The position texture.
     * @param specularTex The specular texture.
     * @param foregroundTex The foreground texture.
     */
    private void render(Texture skyboxTex, Texture diffuseTex, Texture normalTex, Texture reflectiveTex, Texture depthTex, Texture positionTex, Texture specularTex, Texture foregroundTex) {
        client.spriteBatch.enableBlending();

        drawDiffuse(skyboxTex);
        if (client.viewMode == 0) {
            drawDiffuse(diffuseTex);
            drawDiffuse(foregroundTex);

            BlockState buriedBlock = client.player.getBuriedBlock();
            if (!buriedBlock.isAir()) {
                TextureRegion texture = client.getBlockModel(buriedBlock).getBuriedTexture();
                if (!client.player.isSpectator() && texture != null && texture != TextureManager.DEFAULT_TEX_REG && texture.getTexture() != null && texture.getTexture() != TextureManager.DEFAULT_TEX_REG.getTexture()) {
                    client.spriteBatch.draw(texture, 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
                    client.renderer.fill(0, 0, client.getWidth(), client.getHeight(), new Color(0, 0, 0, 0.5f));
                }
            }

            drawDiffuse(vignetteTex, ClientConfig.vignetteOpacity);
        } else if (client.viewMode == 1) {
            drawDiffuse(normalTex);
        } else if (client.viewMode == 2) {
            drawDiffuse(reflectiveTex);
        } else if (client.viewMode == 3) {
            drawDiffuse(depthTex);
        } else if (client.viewMode == 4) {
            drawDiffuse(positionTex);
        } else if (client.viewMode == 5) {
            drawDiffuse(specularTex);
        } else if (client.viewMode == 6) {
            drawDiffuse(foregroundTex);
        } else {
            drawDiffuse(skyboxTex);
            drawDiffuse(foregroundTex);
        }
    }

    /**
     * Draws the diffuse texture.
     *
     * @param diffuseTexture The diffuse texture.
     */
    private void drawDiffuse(@NotNull Texture diffuseTexture) {
        client.spriteBatch.setShader(null);
        client.spriteBatch.draw(diffuseTexture, 0, 0, client.getWidth(), client.getHeight());
    }

    /**
     * Draws the diffuse texture with the given opacity.
     *
     * @param diffuseTexture The diffuse texture.
     * @param opacity The opacity.
     */
    private void drawDiffuse(@NotNull Texture diffuseTexture, float opacity) {
        client.spriteBatch.setShader(null);
        client.spriteBatch.setColor(1, 1, 1, opacity);
        client.spriteBatch.draw(diffuseTexture, 0, 0, client.getWidth(), client.getHeight());
        client.spriteBatch.setColor(1, 1, 1, 1);
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
        stream.println("Shader Handle: " + program.get().getHandle());
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
