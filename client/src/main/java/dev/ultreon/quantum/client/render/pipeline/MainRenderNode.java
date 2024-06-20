package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.render.ShaderPrograms;
import dev.ultreon.quantum.client.render.pipeline.RenderPipeline.RenderNode;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.io.PrintStream;
import java.util.function.Supplier;

import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.*;

public class MainRenderNode extends RenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final Supplier<ShaderProgram> program = ShaderPrograms.MAIN;
    private float blurScale = 0f;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input, float deltaTime) {
        Texture depthTex = textures.get("depth");
        Texture skyboxTex = textures.get("skybox");
        Texture diffuseTex = textures.get("diffuse");
        Texture positionTex = textures.get("position");
        Texture normalTex = textures.get("normal");
        Texture reflectiveTex = textures.get("reflective");
        Texture specularTex = textures.get("specular");

        modelBatch.end();
        this.client.renderer.begin();
        this.client.renderer.getBatch().enableBlending();
        this.client.renderer.getBatch().setBlendFunctionSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);

        if (blurScale > 0f) {
            this.client.renderer.blurred(blurScale, ClientConfig.blurRadius * blurScale, true, 1, () -> {
                render(skyboxTex, diffuseTex, normalTex, reflectiveTex, depthTex, positionTex);
            });
        } else {
            this.client.renderer.blurred(0, ClientConfig.blurRadius * 0, true, 1, () -> {
                render(skyboxTex, diffuseTex, normalTex, reflectiveTex, depthTex, positionTex);
            });
        }
        this.client.renderer.getBatch().disableBlending();
        this.client.renderer.end();
        this.client.spriteBatch.setShader(null);

        gl.glActiveTexture(GL_TEXTURE0);

        if (GamePlatform.get().showRenderPipeline()) {
            this.client.renderer.begin();
            this.client.renderer.getBatch().setBlendFunctionSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
            this.client.spriteBatch.draw(diffuseTex, (float) 0, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(positionTex, (float) (Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(normalTex, (float) (2 *  Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(reflectiveTex, (float) (3 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(skyboxTex, (float) (4 * Gdx.graphics.getWidth()) / 5, 0, (float) Gdx.graphics.getWidth() / 5, (float) Gdx.graphics.getHeight() / 5);
            this.client.renderer.end();
        }

        return input;
    }

    private void render(Texture skyboxTex, Texture diffuseTex, Texture normalTex, Texture reflectiveTex, Texture depthTex, Texture positionTex) {
        this.drawDiffuse(skyboxTex);
        this.client.spriteBatch.flush();
        this.client.modelBatch.flush();
        diffuseTex.bind(0);
        positionTex.bind(1);
        normalTex.bind(2);
        depthTex.bind(3);
        reflectiveTex.bind(4);

        FrameBuffer frameBuffer = this.getFrameBuffer();
        frameBuffer.begin();

        ShaderProgram program = this.program.get();
        program.setUniformi("uPosition", 0);
        program.setUniformi("uNormal", 1);
//        program.setUniformi("uDiffuse", 2);
        program.setUniformi("uReflective", 3);
        program.setUniformMatrix("view", client.camera.view);
        program.setUniformf("maxDistance", ClientConfig.maxReflectDistance);
        program.setUniformf("thickness", 0.5f);
        program.setUniformf("resolution", 1.0f);
        quad.render(program, GL_TRIANGLES);

        frameBuffer.end();
        Texture colorBufferTexture = frameBuffer.getColorBufferTexture();
        this.client.spriteBatch.draw(colorBufferTexture, 0, 0);
        this.client.spriteBatch.flush();
    }

    private void drawDiffuse(Texture diffuseTexture) {
        this.client.spriteBatch.setShader(this.program.get());
        this.client.spriteBatch.draw(diffuseTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
