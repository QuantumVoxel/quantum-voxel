package dev.ultreon.quantum.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
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
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;

public class MainRenderNode extends RenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final Supplier<ShaderProgram> program = ShaderPrograms.MODEL;
    private float blurScale = 0f;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        Texture depthMap = textures.get("depth");
        Texture skyboxTexture = textures.get("skybox");
        Texture diffuseTexture = textures.get("diffuse");

        diffuseTexture.bind(0);

        modelBatch.end();
        this.client.renderer.begin();

        if (blurScale > 0f) {
            this.client.renderer.blurred(blurScale, ClientConfig.blurRadius * blurScale, true, 1, () -> {
                this.drawDiffuse(skyboxTexture);
                this.drawDiffuse(diffuseTexture);
            });
        } else {
            this.drawDiffuse(skyboxTexture);
            this.drawDiffuse(diffuseTexture);
        }
        this.client.renderer.end();
        this.client.spriteBatch.setShader(null);

        gl.glActiveTexture(GL_TEXTURE0);

        if (GamePlatform.get().showRenderPipeline()) {
            this.client.renderer.begin();
            this.client.spriteBatch.draw(diffuseTexture, (float) (2 * Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(skyboxTexture, (float) Gdx.graphics.getWidth() / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
            this.client.renderer.end();
        }

        return input;
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
