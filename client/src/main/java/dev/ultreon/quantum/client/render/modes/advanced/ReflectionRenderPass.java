package dev.ultreon.quantum.client.render.modes.advanced;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
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
import org.jetbrains.annotations.Nullable;

public class ReflectionRenderPass extends RenderPass {
    private Texture @Nullable [] textures;
    private @Nullable ShaderProgram reflectionShader;
    private @Nullable FrameBuffer reflectionFbo;
    private boolean enabled = false;
    private final SpriteBatch spriteBatch = GamePlatform.get().createSpriteBatch();
    private final WorldRenderPass worldPass;
    private final Matrix4 invProj = new Matrix4();
    private final Matrix4 invView = new Matrix4();

    public ReflectionRenderPass(WorldRenderPass worldPass) {
        super("Reflection (Advanced)");
        this.worldPass = worldPass;
    }

    @Override
    protected void create() {
        this.reflectionShader = new ShaderProgram(
                QuantumClient.resource(NamespaceID.of("shaders/basic/flat.glsl")),
                QuantumClient.resource(NamespaceID.of("shaders/postprocessing/ssr_reflect.glsl"))
        );

        if (!reflectionShader.isCompiled()) {
            CommonConstants.LOGGER.error("Could not compile post-processing shader:\n" + reflectionShader.getLog());
            throw new RuntimeException("Could not compile post-processing shader:\n" + reflectionShader.getLog());
        }

        if (reflectionFbo != null) reflectionFbo.dispose();
        this.reflectionFbo = new NestableFrameBuffer.NestableFrameBufferBuilder(getWidth(), getHeight())
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                .addBasicStencilDepthPackedRenderBuffer()
                .build();

        this.textures = new Texture[]{reflectionFbo.getColorBufferTexture()};
    }

    @Override
    public void dispose() {
        if (!enabled) return;
        if (reflectionShader != null) reflectionShader.dispose();
        if (reflectionFbo != null) reflectionFbo.dispose();
        enabled = false;
    }

    @Override
    protected void resize(int newWidth, int newHeight) {
        if (reflectionFbo != null) reflectionFbo.dispose();
        reflectionFbo = new NestableFrameBuffer.NestableFrameBufferBuilder(getWidth(), getHeight())
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                .addBasicStencilDepthPackedRenderBuffer()
                .build();
    }

    @Override
    public void render(RenderBufferSource bufferSource, RenderContext context) {
        if (!enabled) return;
        if (reflectionShader == null || reflectionFbo == null) return;


        reflectionFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);
        this.spriteBatch.begin();
        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
        Gdx.gl.glStencilMask(0x00);
        this.spriteBatch.setShader(this.reflectionShader);
        worldPass.getTextures()[4].bind(2);
        worldPass.getTextures()[3].bind(4);
        worldPass.getTextures()[2].bind(3);
        worldPass.getTextures()[1].bind(1);
        worldPass.getTextures()[0].bind(0);
        this.reflectionShader.setUniformi("colorBuffer", 0);
        this.reflectionShader.setUniformi("gNormal", 1);
        this.reflectionShader.setUniformi("depthMap", 2);
        this.reflectionShader.setUniformi("gReflection", 3);
        this.reflectionShader.setUniformi("gRoughness", 4);
        this.reflectionShader.setUniformf("near", context.camera.near);
        this.reflectionShader.setUniformf("far", context.camera.far);
        this.reflectionShader.setUniformf("topColor", context.skybox.topColor);
        this.reflectionShader.setUniformf("bottomColor", context.skybox.bottomColor);
        this.reflectionShader.setUniformf("midColor", context.skybox.midColor);
        this.reflectionShader.setUniformMatrix("invProjection", invProj.set(context.camera.projection).inv());
        this.reflectionShader.setUniformMatrix("invViewMatrix", invView.set(context.camera.view).inv());
        this.reflectionShader.setUniformMatrix("projection", context.camera.projection);
        this.reflectionShader.setUniformf("SCR_WIDTH", getWidth());
        this.reflectionShader.setUniformf("SCR_HEIGHT", getHeight());
        this.spriteBatch.draw(worldPass.getTextures()[0], 0, getHeight(), getWidth(), -getHeight());
        this.spriteBatch.end();
        reflectionFbo.end();
    }

    @Override
    public @Nullable RenderType renderTypeFor(RenderMaterial renderMaterial) {
        return null;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public Texture[] getTextures() {
        return textures;
    }
}
