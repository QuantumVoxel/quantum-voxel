package dev.ultreon.quantum.client.render.modes.advanced;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import de.damios.guacamole.gdx.graphics.NestableFrameBuffer;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.client.render.context.RenderContext;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OutputRenderPass extends RenderPass {
    private final WorldRenderPass worldPass;
    private final ReflectionRenderPass reflectionPass;
    private @Nullable ShaderProgram outputShader;
    private @Nullable FrameBuffer outFbo;
    private boolean enabled = false;
    private final SpriteBatch spriteBatch = GamePlatform.get().createSpriteBatch();

    protected OutputRenderPass(WorldRenderPass worldPassIn, ReflectionRenderPass reflectionPassIn) {
        super("Output");
        worldPass = worldPassIn;
        reflectionPass = reflectionPassIn;
    }

    @Override
    protected void create() {
        if (enabled) return;
        enabled = true;
        outputShader = new ShaderProgram(
                QuantumClient.resource(NamespaceID.of("shaders/basic/flat.glsl")),
                QuantumClient.resource(NamespaceID.of("shaders/postprocessing/output.glsl"))
        );

        if (!outputShader.isCompiled()) {
            CommonConstants.LOGGER.error("Could not compile post-processing shader:\n" + outputShader.getLog());
            throw new RuntimeException("Could not compile post-processing shader:\n" + outputShader.getLog());
        }

        if (outFbo != null) outFbo.dispose();
        outFbo = new NestableFrameBuffer.NestableFrameBufferBuilder(getWidth(), getHeight())
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                .addBasicStencilDepthPackedRenderBuffer()
                .build();
    }

    @Override
    public void dispose() {
        if (!enabled) return;
        if (outputShader != null) outputShader.dispose();
        if (outFbo != null) outFbo.dispose();
        enabled = false;
    }

    @Override
    protected void resize(int newWidth, int newHeight) {
        if (outFbo != null) outFbo.dispose();
        outFbo = new NestableFrameBuffer.NestableFrameBufferBuilder(newWidth, newHeight)
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888) // Color
                .addBasicStencilDepthPackedRenderBuffer()
                .build();

        this.spriteBatch.setProjectionMatrix(spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, newWidth, newHeight));
    }

    @Override
    public void render(RenderBufferSource bufferSource, RenderContext context) {
        if (!enabled) throw notEnabled();

        assert outFbo != null;
        assert outputShader != null;

        outFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);
        Gdx.gl.glStencilMask(0xFF);
        spriteBatch.begin();
        spriteBatch.setShader(outputShader);
        worldPass.getTextures()[0].bind(0);
        reflectionPass.getTextures()[0].bind(1);
        worldPass.getTextures()[2].bind(2);
        outputShader.setUniformi("colorTexture", 0);
        outputShader.setUniformi("refTexture", 1);
        outputShader.setUniformi("maskTexture", 2);
        spriteBatch.draw(worldPass.getTextures()[0], 0, getHeight(), getWidth(), -getHeight());
        spriteBatch.end();
        outFbo.end();

    }

    @Override
    public @Nullable RenderType renderTypeFor(RenderMaterial renderMaterial) {
        return null;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public Texture @NotNull [] getTextures() {
        if (outFbo == null) throw notEnabled();
        return new Texture[]{outFbo.getColorBufferTexture()};
    }
}
