package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.api.events.RenderEvents;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Overlays;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.overlay.OverlayManager;
import dev.ultreon.quantum.client.gui.overlay.wm.WindowManager;
import dev.ultreon.quantum.client.input.TouchInput;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.render.pipeline.RenderPipeline;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.debug.timing.Timing;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.ultreon.quantum.client.QuantumClient.LOGGER;
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
    private final Vector2 tmp = new Vector2();
    private final RenderContext context;
    private float cameraBop = 0.0f;
    private float blurScale = 0.0f;
    private @Nullable Texture vignetteTex;
    private boolean disposed;
    private final LineRenderer lineRenderer = new LineRenderer();
    private final float[] mouseVec = new float[2];
    private final Vector3 tmp3 = new Vector3();
    private Vec3f tmp1 = new Vec3f();

    /**
     * Constructs a new GameRenderer with the specified client, model batch, and render pipeline.
     *
     * @param client     the client instance to be used by the renderer
     * @param modelBatch the batch used for rendering 3D models
     */
    public GameRenderer(QuantumClient client, ModelBatch modelBatch) {
        this.client = client;

        this.context = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.ROUNDROBIN));
    }

    public void resize(int width, int height) {

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
        if (!GamePlatform.get().hasBackPanelRemoved()) {
            renderer.clearColor(0, 0, 0, 1);
        }

        if (player != null) {
            try (var ignored1 = QuantumClient.PROFILER.start("camera")) {
                positionCamera(deltaTime, player);
            }
        }

        client.backgroundCat.update(deltaTime);
        client.mainCat.update(deltaTime);
        client.worldCat.update(deltaTime);

        renderWorld(deltaTime, world, worldRenderer);

        renderer.begin();

        var screen = this.client.screen;


        renderOverlays(renderer, deltaTime, world, worldRenderer, screen);

        renderer.end();
    }

    private void renderOverlays(Renderer renderer, float deltaTime, ClientWorld world, WorldRenderer worldRenderer, Screen screen) {
        renderer.pushMatrix();
        renderer.translate(this.client.getDrawOffset().x, this.client.getDrawOffset().y);
        renderer.scale(this.client.getGuiScale(), this.client.getGuiScale());
        try (var ignored = QuantumClient.PROFILER.start("overlay")) {
            if (!GamePlatform.get().hasBackPanelRemoved() && !(this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed())) {
                renderer.clearColor(1 / 255f, 1 / 255f, 1 / 255f, 1);
            }

            this.renderOverlays(renderer, screen, world, deltaTime);

            if (this.client.crashOverlay != null) {
                if (!GamePlatform.get().isMacOSX() && Gdx.input.isKeyPressed(Input.Keys.F1) && Gdx.input.isKeyPressed(Input.Keys.Q)) {
                    this.client.crashOverlay.render(renderer, deltaTime);
                } else if (GamePlatform.get().isMacOSX() && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.C)) {
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
    }

    private void renderWorld(float deltaTime, ClientWorld world, WorldRenderer worldRenderer) {
        if (this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {

            try (var ignored = QuantumClient.PROFILER.start("world")) {
                RenderEvents.PRE_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);

                var blurScale = this.blurScale;
                blurScale += client.screen != null ? Gdx.graphics.getDeltaTime() * 3f : -Gdx.graphics.getDeltaTime() * 3f;

                blurScale = Mth.clamp(blurScale, 0f, 1f);
                this.blurScale = blurScale;

                this.renderWorld(deltaTime);
                RenderEvents.POST_RENDER_WORLD.factory().onRenderWorld(world, worldRenderer);
            }
        }
    }

    private void positionCamera(float deltaTime, LocalPlayer player) {
        if (this.client.screen == null && Gdx.input.isCursorCatched() || GamePlatform.get().isMobile()) {
            // Calculate delta position for player rotation.
            int width = QuantumClient.get().getWidth();
            int height = QuantumClient.get().getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            if (GamePlatform.get().isMobile()) {
                if (Gdx.input.isTouched()) {
                    float dx = (int) (-Gdx.input.getDeltaX() * ClientConfiguration.cameraSensitivity.getValue());
                    float dy = (int) (-Gdx.input.getDeltaY() * ClientConfiguration.cameraSensitivity.getValue());
                    player.rotateHead(dx, dy);
                }
            } else if (GamePlatform.get().isWeb()) {
                float dx = (int) (-Gdx.input.getDeltaX() * ClientConfiguration.cameraSensitivity.getValue());
                float dy = (int) (-Gdx.input.getDeltaY() * ClientConfiguration.cameraSensitivity.getValue());
                player.rotateHead(dx, dy);
            } else if (GamePlatform.get().isDesktop()) {
                float dx = (int) (-(Gdx.input.getX() - centerX) * ClientConfiguration.cameraSensitivity.getValue());
                float dy = (int) (-(Gdx.input.getY() - centerY) * ClientConfiguration.cameraSensitivity.getValue());
                player.rotateHead(dx, dy);
            }

            // Reset position
            Gdx.input.setCursorPosition(centerX, centerY);
        }

        this.client.camera.update(player);
        this.client.camera.far = ((float) ClientConfiguration.renderDistance.getValue() / CS - 1) * World.CS / WorldRenderer.SCALE;

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
     * @param deltaTime The time elapsed since the last frame.
     */
    void renderWorld(float deltaTime) {
        RenderBufferSource bufferSource = this.client.renderBuffers();
        bufferSource.begin(this.client.camera);

        // Background
        @Nullable ClientWorldAccess world = this.client.world;
        @Nullable TerrainRenderer worldRenderer = this.client.worldRenderer;
        LocalPlayer localPlayer = this.client.player;

        // World
        if (localPlayer == null || worldRenderer == null || world == null) {
            LOGGER.warn("worldRenderer or localPlayer is null");
            return;
        }
        if (this.client.renderWorld) {
            worldRenderer.renderBackground(bufferSource, Gdx.graphics.getDeltaTime());
        }

        bufferSource.getBuffer(RenderPass.SKYBOX).flush();
        bufferSource.getBuffer(RenderPass.CELESTIAL_BODIES).flush();

        var position = localPlayer.getPosition(client.partialTick);
        Array<Entity> toSort = new Array<>(world.getAllEntities());
        worldRenderer.render(client.renderBuffers(), deltaTime);
        toSort.sort((e1, e2) -> {
            var d1 = e1.getPosition().dst(position);
            var d2 = e2.getPosition().dst(position);
            return Double.compare(d1, d2);
        });
        for (Entity entity : toSort.toArray(Entity.class)) {
            if (entity instanceof LocalPlayer) continue;
            worldRenderer.collectEntity(entity, client.renderBuffers());
        }

        // Particles
        ParticleSystem particleSystem = worldRenderer.getParticleSystem();
        particleSystem.begin();
        particleSystem.updateAndDraw(Gdx.graphics.getDeltaTime());
        particleSystem.end();
//        modelBatch.render(particleSystem);
        // TODO add particle system

        // Foreground
        worldRenderer.renderForeground(client.renderBuffers(), deltaTime);

        // Extract textures
        if (vignetteTex == null) {
            vignetteTex = client.getTextureManager().getTexture(new NamespaceID("textures/gui/vignette.png"));
        }

        bufferSource.end();

        // Cursor
        if (client.cursor instanceof BlockHit) {
            BlockHit blockHit = (BlockHit) client.cursor;
            BlockVec blockVec = blockHit.getBlockVec();
            BlockVec next = blockHit.getNext();
            BoundingBox boundingBox = blockHit.getBlockMeta().getBoundingBox(blockVec.x, blockVec.y, blockVec.z);
            Vec3f vec = blockVec.f();
            Vec3f nextVec = next.f();
            lineRenderer.renderBox(client.camera, boundingBox, new Vector3((float) blockHit.getNormal().x, (float) blockHit.getNormal().y, (float) blockHit.getNormal().z), 6, Color.WHITE);
        }
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
        renderHUD(renderer, world, deltaTime);

        if (screen == null) return;

        try (var ignored = QuantumClient.PROFILER.start("screen")) {
            GridPoint2 mouseOffset = this.client.getMousePos();
            mouseVec[0] = GamePlatform.get().isShowingImGui() ? mouseOffset.x / this.client.getGuiScale() : Gdx.input.getX() / this.client.getGuiScale();
            mouseVec[1] = GamePlatform.get().isShowingImGui() ? mouseOffset.y / this.client.getGuiScale() : Gdx.input.getY() / this.client.getGuiScale();

            processMouseCoords(mouseVec);
            RenderEvents.PRE_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, mouseVec[0], mouseVec[1], deltaTime);
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            renderer.getBatch().enableBlending();
            renderer.getBatch().setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);

            Timing.start("screen");
            screen.render(renderer, deltaTime);
            screen.renderTooltips(renderer, (int) mouseVec[0], (int) mouseVec[1], deltaTime);
            Timing.end("screen");

            renderTopOverlay(renderer, screen, deltaTime, mouseVec[0], mouseVec[1]);
        }
    }

    private void processMouseCoords(float[] vec) {
        if (GamePlatform.get().isMobile()) {
            MouseDevice mouseDevice = GamePlatform.get().getMouseDevice();
            if (mouseDevice != null) {
                vec[0] = mouseDevice.getX() / this.client.getGuiScale();
                vec[1] = mouseDevice.getY() / this.client.getGuiScale();
            } else if (TouchInput.isPressingAnyButton()) {
                vec[0] = Gdx.input.getX() / this.client.getGuiScale();
                vec[1] = Gdx.input.getY() / this.client.getGuiScale();
            } else {
                vec[0] = Integer.MIN_VALUE;
                vec[1] = Integer.MIN_VALUE;
            }
        }
    }

    private static void renderTopOverlay(Renderer renderer, @NotNull Screen screen, float deltaTime, float x, float y) {
        WindowManager.render(renderer, (int) x, (int) y, deltaTime);
        renderer.getBatch().enableBlending();
        renderer.flush();

        Overlays.MEMORY.render(renderer, deltaTime);
        RenderEvents.POST_RENDER_SCREEN.factory().onRenderScreen(screen, renderer, x, y, deltaTime);
    }

    private void renderHUD(Renderer renderer, ClientWorldAccess world, float deltaTime) {
        if (world != null) {
            try (var ignored = QuantumClient.PROFILER.start("hud")) {
                OverlayManager.render(renderer, deltaTime);
                RenderEvents.RENDER_OVERLAY.factory().onRenderOverlay(renderer, deltaTime);
            }
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
        if (disposed) return;
        disposed = true;

        lineRenderer.dispose();
        if (vignetteTex != null) vignetteTex.dispose();
    }
}
