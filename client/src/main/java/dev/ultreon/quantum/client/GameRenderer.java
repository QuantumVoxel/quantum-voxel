package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.GridPoint2;
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
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.overlay.wm.WindowManager;
import dev.ultreon.quantum.client.input.TouchInput;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.pipeline.RenderPipeline;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

import static dev.ultreon.quantum.world.World.CS;

/**
 * The GameRenderer class is responsible for rendering the game world and overlays.
 * It handles the rendering pipeline, update cycles, and camera adjustments.
 * <p>
 * This class is not thread-safe. And should only be used on the main/render thread.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
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

    /**
     * Constructs a new GameRenderer with the specified client, model batch, and render pipeline.
     *
     * @param client the client instance to be used by the renderer
     * @param modelBatch the batch used for rendering 3D models
     * @param pipeline the rendering pipeline managing different render passes
     */
    public GameRenderer(QuantumClient client, ModelBatch modelBatch, RenderPipeline pipeline) {
        this.client = client;
        this.modelBatch = modelBatch;
        this.pipeline = pipeline;

        this.context = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN));

        this.depthFbo = new FrameBuffer(Pixmap.Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        this.depthFbo.dispose();
        this.fbo.dispose();
        this.depthFbo = new FrameBuffer(Pixmap.Format.RGBA8888, QuantumClient.get().getWidth(), QuantumClient.get().getHeight(), true);
        this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, QuantumClient.get().getWidth(), QuantumClient.get().getHeight(), true);
        this.pipeline.resize(width, height);
    }

    /**
     * Renders the game state including the world, overlays, and notifications.
     *
     * @param renderer The renderer used for rendering the game.
     * @param deltaTime The time elapsed since the last frame.
     */
    public void render(Renderer renderer, float deltaTime) {
        var world = this.client.world;
        var worldRenderer = this.client.worldRenderer;

        LocalPlayer player = this.client.player;
        if (!(GamePlatform.get().hasBackPanelRemoved())) {
            renderer.clearColor(0, 0, 0, 1);
        }

        if (player != null) {
            try (var ignored1 = QuantumClient.PROFILER.start("camera")) {
                if (this.client.screen == null && Gdx.input.isCursorCatched()) {
                    // Calculate delta position for player rotation.
                    int width = QuantumClient.get().getWidth();
                    int height = QuantumClient.get().getHeight();
                    int centerX = width / 2;
                    int centerY = height / 2;
                    float dx = (int) (-(Gdx.input.getX() - centerX) * ClientConfig.cameraSensitivity);
                    float dy = (int) (-(Gdx.input.getY() - centerY) * ClientConfig.cameraSensitivity);
                    player.rotateHead(dx, dy);

                    // Reset position
                    Gdx.input.setCursorPosition(centerX, centerY);
                }

                this.client.camera.update(player);
                this.client.camera.far = ((float) ClientConfig.renderDistance / CS - 1) * World.CS / WorldRenderer.SCALE;

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
            };
        }

        client.backgroundCat.update(deltaTime);
        client.mainCat.update(deltaTime);
        client.worldCat.update(deltaTime);

        if (this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {

            try (var ignored = QuantumClient.PROFILER.start("world")) {
                RenderEvents.PRE_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);

                var blurScale = this.blurScale;
                blurScale += client.screen != null ? Gdx.graphics.getDeltaTime() * 3f : -Gdx.graphics.getDeltaTime() * 3f;

                blurScale = Mth.clamp(blurScale, 0f, 1f);
                this.blurScale = blurScale;

                this.renderWorld(Math.max(blurScale, 0f), deltaTime);
                RenderEvents.POST_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);
            }
        }

        renderer.begin();

        var screen = this.client.screen;


        renderer.pushMatrix();
        renderer.translate(this.client.getDrawOffset().x, this.client.getDrawOffset().y);
        renderer.scale(this.client.getGuiScale(), this.client.getGuiScale());
        try (var ignored = QuantumClient.PROFILER.start("overlay")) {
            if (!GamePlatform.get().hasBackPanelRemoved() && !(this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed())) {
                renderer.clearColor(1 / 255f, 1 / 255f, 1 / 255f, 1);
            }

            this.renderOverlays(renderer, screen, world, deltaTime);

            if (this.client.crashOverlay != null) {
                if (Gdx.input.isKeyPressed(Input.Keys.F1) && Gdx.input.isKeyPressed(Input.Keys.Q)) {
                    this.client.crashOverlay.render(renderer, deltaTime);
                } else {
                    this.client.crashOverlay.reset();
                }
            }
        }

        if (!this.client.isLoading()) {
            this.client.notifications.render(renderer, deltaTime);
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

    /**
     * Renders the world with the given blur scale (for when a screen is open) and delta time.
     * This uses the {@link RenderPipeline} to render the world.
     * 
     * @param blurScale The blur scale.
     * @param deltaTime The time elapsed since the last frame.
     */
    void renderWorld(float blurScale, float deltaTime) {
        this.pipeline.render(this.modelBatch, blurScale, deltaTime);
    }

    /**
     * Renders the overlays.
     *
     * @param renderer The renderer used for rendering the overlays.
     * @param screen The screen to render the overlays on.
     * @param world The world to render the overlays on.
     * @param deltaTime The time elapsed since the last frame.
     */
    private void renderOverlays(Renderer renderer, @Nullable Screen screen, ClientWorldAccess world, float deltaTime) {
        if (world != null) {
            try (var ignored = QuantumClient.PROFILER.start("hud")) {
                if (this.client.hideHud) return;
                OverlayManager.render(renderer, deltaTime);
                RenderEvents.RENDER_OVERLAY.factory().onRenderOverlay(renderer, deltaTime);
            }
        }

        if (screen != null) {
            try (var ignored = QuantumClient.PROFILER.start("screen")) {
                GridPoint2 mouseOffset = this.client.getMousePos();
                float x = GamePlatform.get().isShowingImGui() ? mouseOffset.x / this.client.getGuiScale() : Gdx.input.getX() / this.client.getGuiScale();
                float y = GamePlatform.get().isShowingImGui() ? mouseOffset.y / this.client.getGuiScale() : Gdx.input.getY() / this.client.getGuiScale();

                if (GamePlatform.get().isMobile()) {
                    MouseDevice mouseDevice = GamePlatform.get().getMouseDevice();
                    if (mouseDevice != null) {
                        x = mouseDevice.getX() / this.client.getGuiScale();
                        y = mouseDevice.getY() / this.client.getGuiScale();
                    } else if (TouchInput.isPressingAnyButton()) {
                        x = Gdx.input.getX() / this.client.getGuiScale();
                        y = Gdx.input.getY() / this.client.getGuiScale();
                    } else {
                        x = Integer.MIN_VALUE;
                        y = Integer.MIN_VALUE;
                    }
                }
                RenderEvents.PRE_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, x, y, deltaTime);
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                renderer.getBatch().enableBlending();
                renderer.getBatch().setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);

                screen.render(renderer, deltaTime);
                screen.renderTooltips(renderer, (int) x, (int) y, deltaTime);
                WindowManager.render(renderer, (int) x, (int) y, deltaTime);
                renderer.getBatch().enableBlending();
                renderer.flush();

                Overlays.MEMORY.render(renderer, deltaTime);
                RenderEvents.POST_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, x, y, deltaTime);
            }
        }

        try (var ignored = QuantumClient.PROFILER.start("debug")) {
            if (this.client.hideHud || this.client.isLoading()) return;
            this.client.debugGui.render(renderer);
        }
    }

    /**
     * Gets the render context.
     * 
     * @return The render context.
     */
    public RenderContext getContext() {
        return this.context;
    }

    /**
     * Disposes of the GameRenderer.
     * 
     * @see #dispose()
     */
    @Override
    public void dispose() {
        this.depthFbo.dispose();
        this.fbo.dispose();
    }
}
