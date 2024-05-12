package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.api.events.RenderEvents;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Overlays;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.input.TouchscreenInput;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.Scene3D;
import dev.ultreon.quantum.client.render.ShaderPrograms;
import dev.ultreon.quantum.client.render.pipeline.RenderPipeline;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

import static dev.ultreon.quantum.client.QuantumClient.LOGGER;

public class GameRenderer implements Disposable {
    private final QuantumClient client;
    private final ModelBatch modelBatch;
    private final RenderPipeline pipeline;
    private final Vector2 tmp = new Vector2();
    private FrameBuffer depthFbo;
    private FrameBuffer fbo;
    private final RenderContext context;
    private float cameraBop = 0.0f;
    private float blurScale = 0.0f;

    public GameRenderer(QuantumClient client, ModelBatch modelBatch, RenderPipeline pipeline) {
        this.client = client;
        this.modelBatch = modelBatch;
        this.pipeline = pipeline;

        this.context = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN));

        this.depthFbo = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        ShaderProgram worldShaderProgram = ShaderPrograms.MODEL.get();
        if (!worldShaderProgram.isCompiled()) {
            LOGGER.error("Failed to compile model shader:\n%s", worldShaderProgram.getLog());
        }
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        this.depthFbo.dispose();
        this.fbo.dispose();
        this.depthFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.pipeline.resize(width, height);
    }

    public void render(Renderer renderer, float deltaTime) {
        var world = this.client.world;
        var worldRenderer = this.client.worldRenderer;

        LocalPlayer player = this.client.player;

        if (player != null) {
            QuantumClient.PROFILER.section("camera", () -> {
                if (this.client.screen == null && !GamePlatform.get().isShowingImGui()) {
                    player.rotateHead(-Gdx.input.getDeltaX() * ClientConfig.cameraSensitivity, -Gdx.input.getDeltaY() * ClientConfig.cameraSensitivity);
                }

                this.client.camera.update(player);
                this.client.camera.far = (ClientConfig.renderDistance - 1) * World.CHUNK_SIZE / WorldRenderer.SCALE;

                var rotation = this.tmp.set(player.xHeadRot, player.yRot);
                var quaternion = new Quaternion();
                quaternion.setFromAxis(Vector3.Y, rotation.x);
                quaternion.mul(new Quaternion(Vector3.X, rotation.y));
                quaternion.conjugate();

                // Add camera bop. Use easing and animate with cameraBop. Camera Bop is a sort of camera movement while walking.
                float cameraBop = calculateCameraBop(deltaTime);

                this.client.camera.up.set(0, 1, 0);
                this.client.camera.up.rotate(Vector3.Y, rotation.x);
                this.client.camera.up.rotate(Vector3.Z, cameraBop);
                this.client.camera.up.rotate(Vector3.Y, -rotation.x);
            });
        }

        Scene3D.BACKGROUND.update(deltaTime);
        Scene3D.WORLD.update(deltaTime);

        if (this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {

            QuantumClient.PROFILER.section("world", () -> {
                RenderEvents.PRE_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);

                var blurScale = this.blurScale;
                blurScale += client.screen != null ? Gdx.graphics.getDeltaTime() * 3f : -Gdx.graphics.getDeltaTime() * 3f;

                blurScale = Mth.clamp(blurScale, 0f, 1f);
                this.blurScale = blurScale;

                this.renderWorld(Math.max(blurScale, 0f));
                RenderEvents.POST_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);
            });
        }

        renderer.begin();

        var screen = this.client.screen;


        renderer.pushMatrix();
        renderer.translate(this.client.getDrawOffset().x, this.client.getDrawOffset().y);
        renderer.scale(this.client.getGuiScale(), this.client.getGuiScale());
        QuantumClient.PROFILER.section("overlay", () -> {
            this.renderOverlays(renderer, screen, world, deltaTime);

            if (this.client.crashOverlay != null) {
                if (Gdx.input.isKeyPressed(Input.Keys.F1) && Gdx.input.isKeyPressed(Input.Keys.Q)) {
                    this.client.crashOverlay.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
                } else {
                    this.client.crashOverlay.reset();
                }
            }
        });

        if (!this.client.isLoading()) {
            this.client.notifications.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
        }

        renderer.popMatrix();

        renderer.end();
    }

    /**
     * Calculates the camera bop movement based on the given deltaTime.
     *
     * @param deltaTime the time elapsed since the last frame
     * @return the calculated camera bop value
     */
    private float calculateCameraBop(float deltaTime) {
        float bop = this.cameraBop;
        if (bop > 0) {
            bop -= deltaTime * 2;
            if (bop < 0) bop = 0;
        } else if (bop < 0) {
            bop += deltaTime * 2;
            if (bop > 0) bop = 0;
        }

        return this.cameraBop = bop;
    }

    void renderWorld(float blurScale) {
        this.pipeline.render(this.modelBatch, blurScale);
    }

    private void renderOverlays(Renderer renderer, @Nullable Screen screen, @Nullable World world, float deltaTime) {
        if (world != null) {
            QuantumClient.PROFILER.section("hud", () -> {
                if (this.client.hideHud) return;
                OverlayManager.render(renderer, deltaTime);
                RenderEvents.RENDER_OVERLAY.factory().onRenderOverlay(renderer, deltaTime);
            });
        }

        if (screen != null) {
            QuantumClient.PROFILER.section("screen", () -> {
                float x = (Gdx.input.getX() - this.client.getDrawOffset().x) / this.client.getGuiScale();
                float y = (Gdx.input.getY() + this.client.getDrawOffset().y) / this.client.getGuiScale();

                if (GamePlatform.get().isMobile()) {
                    MouseDevice mouseDevice = GamePlatform.get().getMouseDevice();
                    if (mouseDevice != null) {
                        x = mouseDevice.getX() / this.client.getGuiScale();
                        y = mouseDevice.getY() / this.client.getGuiScale();
                    } else if (TouchscreenInput.isPressingAnyButton()) {
                        x = Gdx.input.getX() / this.client.getGuiScale();
                        y = Gdx.input.getY() / this.client.getGuiScale();
                    } else {
                        x = Integer.MIN_VALUE;
                        y = Integer.MIN_VALUE;
                    }
                }
                RenderEvents.PRE_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, x, y, deltaTime);
                screen.render(renderer, (int) x, (int) y, deltaTime);

                Overlays.MEMORY.render(renderer, deltaTime);
                RenderEvents.POST_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, x, y, deltaTime);
            });
        }

        QuantumClient.PROFILER.section("debug", () -> {
            if (this.client.hideHud || this.client.isLoading()) return;
            this.client.debugGui.render(renderer);
        });
    }

    public RenderContext getContext() {
        return this.context;
    }

    @Override
    public void dispose() {
        this.depthFbo.dispose();
        this.fbo.dispose();
    }
}
