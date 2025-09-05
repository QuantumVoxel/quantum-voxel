package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import de.damios.guacamole.gdx.graphics.NestableFrameBuffer;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PostProcessor implements Disposable {
    private int width;
    private int height;
    @Nullable
    private FrameBuffer worldFbo;
    @Nullable
    private FrameBuffer reflectionFbo;
    @Nullable
    private FrameBuffer blurFbo;
    @Nullable
    private FrameBuffer outFbo;
    private final ShaderProgram reflectionShader;
    private final ShaderProgram boxBlurShader;
    private final ShaderProgram outputShader;
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final Matrix4 invProj = new Matrix4();
    private final Matrix4 invView = new Matrix4();

    public PostProcessor() {
        this.reflectionShader = new ShaderProgram(
                QuantumClient.resource(NamespaceID.of("shaders/basic/flat.glsl")),
                QuantumClient.resource(NamespaceID.of("shaders/postprocessing/ssr_reflect.glsl"))
        );

        if (!reflectionShader.isCompiled()) {
            CommonConstants.LOGGER.error("Could not compile post-processing shader:\n" + reflectionShader.getLog());
            throw new RuntimeException("Could not compile post-processing shader:\n" + reflectionShader.getLog());
        }

        this.boxBlurShader = new ShaderProgram(
                QuantumClient.resource(NamespaceID.of("shaders/basic/flat.glsl")),
                QuantumClient.resource(NamespaceID.of("shaders/postprocessing/box_blur.glsl"))
        );

        if (!boxBlurShader.isCompiled()) {
            CommonConstants.LOGGER.error("Could not compile post-processing shader:\n" + boxBlurShader.getLog());
            throw new RuntimeException("Could not compile post-processing shader:\n" + boxBlurShader.getLog());
        }

        this.outputShader = new ShaderProgram(
                QuantumClient.resource(NamespaceID.of("shaders/basic/flat.glsl")),
                QuantumClient.resource(NamespaceID.of("shaders/postprocessing/output.glsl"))
        );

        if (!outputShader.isCompiled()) {
            CommonConstants.LOGGER.error("Could not compile post-processing shader:\n" + outputShader.getLog());
            throw new RuntimeException("Could not compile post-processing shader:\n" + outputShader.getLog());
        }
    }

    public void begin(int width, int height) {
        FrameBuffer frameBuffer = this.worldFbo;
        if (this.width != width || this.height != height || frameBuffer == null) {
            this.width = width;
            this.height = height;
            if (frameBuffer != null) frameBuffer.dispose();
            this.worldFbo = frameBuffer = new NestableFrameBuffer.NestableFrameBufferBuilder(width, height)
                    .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                    .addColorTextureAttachment(GL32.GL_RGBA16F, GL32.GL_RGBA, GL32.GL_FLOAT) // Normal
                    .addBasicColorTextureAttachment(Pixmap.Format.RGB888) // MRT
                    .addBasicColorTextureAttachment(Pixmap.Format.RGB888) // Roughness
                    .addDepthTextureAttachment(GL32.GL_DEPTH32F_STENCIL8, GL32.GL_FLOAT)
                    .build();
            if (reflectionFbo != null) reflectionFbo.dispose();
            this.reflectionFbo = new NestableFrameBuffer.NestableFrameBufferBuilder(width, height)
                    .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                    .addBasicStencilDepthPackedRenderBuffer()
                    .build();
            if (blurFbo != null) reflectionFbo.dispose();
            this.blurFbo = new NestableFrameBuffer.NestableFrameBufferBuilder(width, height)
                    .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                    .addBasicStencilDepthPackedRenderBuffer()
                    .build();
            if (outFbo != null) outFbo.dispose();
            this.outFbo = new NestableFrameBuffer.NestableFrameBufferBuilder(width, height)
                    .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                    .addBasicStencilDepthPackedRenderBuffer()
                    .build();
        }

        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
    }

    public void end() {
        Objects.requireNonNull(this.worldFbo).end();
    }

    public void draw(WorldRenderer renderer) {
        FrameBuffer worldFbo = this.worldFbo;
        if (worldFbo == null) {
            CommonConstants.LOGGER.error("Could not draw post-processing: world frame buffer is null");
            return;
        }

        FrameBuffer reflectionFbo = this.reflectionFbo;
        if (reflectionFbo == null) {
            CommonConstants.LOGGER.error("Could not draw post-processing: reflection frame buffer is null");
            return;
        }

        FrameBuffer blurFbo = this.blurFbo;
        if (blurFbo == null) {
            CommonConstants.LOGGER.error("Could not draw post-processing: reflection frame buffer is null");
            return;
        }

        FrameBuffer outFbo = this.outFbo;
        if (outFbo == null) {
            CommonConstants.LOGGER.error("Could not draw post-processing: output frame buffer is null");
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_0)) {
            this.spriteBatch.setShader(null);
            this.spriteBatch.begin();
            this.spriteBatch.draw(worldFbo.getTextureAttachments().get(0), 0, height, width, -height);
            this.spriteBatch.end();
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_1)) {
            this.spriteBatch.setShader(null);
            this.spriteBatch.begin();
            this.spriteBatch.draw(worldFbo.getTextureAttachments().get(1), 0, height, width, -height);
            this.spriteBatch.end();
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_2)) {
            this.spriteBatch.setShader(null);
            this.spriteBatch.begin();
            this.spriteBatch.draw(worldFbo.getTextureAttachments().get(2), 0, height, width, -height);
            this.spriteBatch.end();
            return;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_3)) {
            this.spriteBatch.setShader(null);
            this.spriteBatch.begin();
            this.spriteBatch.draw(worldFbo.getTextureAttachments().get(3), 0, height, width, -height);
            this.spriteBatch.end();
            return;
        }

        reflectionFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);
        this.spriteBatch.begin();
        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
        Gdx.gl.glStencilMask(0x00);
        this.spriteBatch.setShader(this.reflectionShader);
        worldFbo.getTextureAttachments().get(4).bind(2);
        worldFbo.getTextureAttachments().get(3).bind(4);
        worldFbo.getTextureAttachments().get(2).bind(3);
        worldFbo.getTextureAttachments().get(1).bind(1);
        worldFbo.getTextureAttachments().get(0).bind(0);
        this.reflectionShader.setUniformi("colorBuffer", 0);
        this.reflectionShader.setUniformi("gNormal", 1);
        this.reflectionShader.setUniformi("depthMap", 2);
        this.reflectionShader.setUniformi("gReflection", 3);
        this.reflectionShader.setUniformi("gRoughness", 4);
        this.reflectionShader.setUniformf("near", renderer.getCamera().near);
        this.reflectionShader.setUniformf("far", renderer.getCamera().far);
        this.reflectionShader.setUniformf("topColor", renderer.getSkybox().topColor);
        this.reflectionShader.setUniformf("bottomColor", renderer.getSkybox().bottomColor);
        this.reflectionShader.setUniformf("midColor", renderer.getSkybox().midColor);
        this.reflectionShader.setUniformMatrix("invProjection", invProj.set(renderer.getCamera().projection).inv());
        this.reflectionShader.setUniformMatrix("invViewMatrix", invView.set(renderer.getCamera().view).inv());
        this.reflectionShader.setUniformMatrix("projection", renderer.getCamera().projection);
        this.reflectionShader.setUniformf("SCR_WIDTH", width);
        this.reflectionShader.setUniformf("SCR_HEIGHT", height);
        this.spriteBatch.draw(worldFbo.getTextureAttachments().get(0), 0, height, width, -height);
        this.spriteBatch.end();
        reflectionFbo.end();

        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_4)) {
            this.spriteBatch.setShader(null);
            reflectionFbo.getColorBufferTexture().bind(0);
            this.spriteBatch.begin();
            this.spriteBatch.draw(reflectionFbo.getColorBufferTexture(), 0, height, width, -height);
            this.spriteBatch.end();
            return;
        }

//        blurFbo.begin();
//        Gdx.gl.glClearColor(0, 0, 0, 0);
//        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);
//        this.spriteBatch.begin();
//        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
//        Gdx.gl.glStencilMask(0x00);
//        this.spriteBatch.setShader(this.boxBlurShader);
//        reflectionFbo.getTextureAttachments().get(0).bind(0);
//        this.boxBlurShader.setUniformi("colorTexture", 0);
//        this.boxBlurShader.setUniformf("parameters", 5, 1);
//        this.spriteBatch.draw(reflectionFbo.getTextureAttachments().get(0), 0, height, width, -height);
//        this.spriteBatch.end();
//        blurFbo.end();
//
//        if (Gdx.input.isKeyPressed(Input.Keys.NUMPAD_5)) {
//            this.spriteBatch.setShader(null);
//            blurFbo.getColorBufferTexture().bind(0);
//            this.spriteBatch.begin();
//            this.spriteBatch.draw(blurFbo.getColorBufferTexture(), 0, height, width, -height);
//            this.spriteBatch.end();
//            return;
//        }

        outFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);
        Gdx.gl.glStencilMask(0xFF);
        this.spriteBatch.begin();
        this.spriteBatch.setShader(this.outputShader);
        worldFbo.getTextureAttachments().get(0).bind(0);
        reflectionFbo.getColorBufferTexture().bind(1);
        worldFbo.getTextureAttachments().get(2).bind(2);
        this.outputShader.setUniformi("colorTexture", 0);
        this.outputShader.setUniformi("refTexture", 1);
        this.outputShader.setUniformi("maskTexture", 2);
        this.spriteBatch.draw(worldFbo.getTextureAttachments().get(0), 0, height, width, -height);
        this.spriteBatch.end();
        outFbo.end();

        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
        this.spriteBatch.setShader(null);
        outFbo.getColorBufferTexture().bind(0);
        this.spriteBatch.begin();
        this.spriteBatch.draw(outFbo.getColorBufferTexture(), 0, height, width, -height);
        this.spriteBatch.end();
    }

    @Override
    public void dispose() {
        if (this.worldFbo != null) {
            this.worldFbo.dispose();
            this.worldFbo = null;
        }
        if (this.reflectionFbo != null) {
            this.reflectionFbo.dispose();
            this.reflectionFbo = null;
        }
        if (this.outFbo != null) {
            this.outFbo.dispose();
            this.outFbo = null;
        }
        this.reflectionShader.dispose();
    }
}
