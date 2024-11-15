package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
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

public class MainRenderNode extends RenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final Supplier<ShaderProgram> program = ShaderPrograms.MAIN;
    private float blurScale = 0f;
    private Texture vignetteTex;

    /**
     * Renders the scene with the given textures and parameters.
     *
     * @param textures   The map containing textures.
     * @param modelBatch The ModelBatch used for rendering.
     * @param camera     The GameCamera representing the camera.
     * @param deltaTime  The time passed since the last frame.
     */
    @NewInstance
    @Override
    public void render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, float deltaTime) {
        // Extract textures
        if (vignetteTex == null) {
            vignetteTex = this.client.getTextureManager().getTexture(new NamespaceID("textures/gui/vignette.png"));
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
        modelBatch.end();
        this.client.renderer.begin();
        this.client.renderer.getBatch().enableBlending();
        this.client.renderer.getBatch().setBlendFunctionSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Handle blur effect
        if (blurScale > 0f) {
            this.client.renderer.blurred(blurScale, ClientConfig.blurRadius * blurScale, true, 1, () -> {
                render(skyboxTex, diffuseTex, normalTex, reflectiveTex, depthTex, positionTex, specularTex, foregroundTex);
            });
        } else {
            render(skyboxTex, diffuseTex, normalTex, reflectiveTex, depthTex, positionTex, specularTex, foregroundTex);
        }

        // Disable blending and end rendering
        this.client.renderer.getBatch().disableBlending();
        this.client.renderer.end();
        this.client.spriteBatch.setShader(null);

        gl.glActiveTexture(GL_TEXTURE0);

        // Show render pipeline
        if (GamePlatform.get().showRenderPipeline()) {
            this.client.renderer.begin();
            this.client.renderer.getBatch().enableBlending();
            this.client.spriteBatch.draw(diffuseTex, (float) 0, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(positionTex, (float) (Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(normalTex, (float) (2 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(foregroundTex, (float) (3 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(skyboxTex, (float) (4 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.renderer.end();
        }

        // Enable blending and set blend function
        this.client.renderer.getBatch().enableBlending();
    }

    private void render(Texture skyboxTex, Texture diffuseTex, Texture normalTex, Texture reflectiveTex, Texture depthTex, Texture positionTex, Texture specularTex, Texture foregroundTex) {
        this.client.spriteBatch.enableBlending();

        this.drawDiffuse(skyboxTex);
        if (this.client.viewMode == 0) {
            this.drawDiffuse(diffuseTex);
            this.drawDiffuse(foregroundTex);

            BlockState buriedBlock = this.client.player.getBuriedBlock();
            if (!buriedBlock.isAir()) {
                TextureRegion texture = this.client.getBlockModel(buriedBlock).getBuriedTexture();
                if (!this.client.player.isSpectator() && texture != null && texture != TextureManager.DEFAULT_TEX_REG && texture.getTexture() != null && texture.getTexture() != TextureManager.DEFAULT_TEX_REG.getTexture()) {
                    this.client.spriteBatch.draw(texture, 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
                    this.client.renderer.fill(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new Color(0, 0, 0, 0.5f));
                }
            }

            this.drawDiffuse(vignetteTex, ClientConfig.vignetteOpacity);
        } else if (this.client.viewMode == 1) {
            this.drawDiffuse(normalTex);
        } else if (this.client.viewMode == 2) {
            this.drawDiffuse(reflectiveTex);
        } else if (this.client.viewMode == 3) {
            this.drawDiffuse(depthTex);
        } else if (this.client.viewMode == 4) {
            this.drawDiffuse(positionTex);
        } else if (this.client.viewMode == 5) {
            this.drawDiffuse(specularTex);
        } else if (this.client.viewMode == 6) {
            this.drawDiffuse(foregroundTex);
        } else {
            this.drawDiffuse(skyboxTex);
            this.drawDiffuse(foregroundTex);
        }
    }

    private void drawDiffuse(@NotNull Texture diffuseTexture) {
        this.client.spriteBatch.setShader(null);
        this.client.spriteBatch.draw(diffuseTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void drawDiffuse(@NotNull Texture diffuseTexture, float opacity) {
        this.client.spriteBatch.setShader(null);
        this.client.spriteBatch.setColor(1, 1, 1, opacity);
        this.client.spriteBatch.draw(diffuseTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.client.spriteBatch.setColor(1, 1, 1, 1);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        this.quad.dispose();
        this.quad = this.createFullScreenQuad();
    }

    @Override
    public void dumpInfo(PrintStream stream) {
        super.dumpInfo(stream);
        stream.println("Shader Handle: " + this.program.get().getHandle());
    }

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

    public void blur(float blurScale) {
        this.blurScale = blurScale;
    }
}
