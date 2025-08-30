package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.DevFlag;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.client.api.events.RenderHudEvent;
import dev.ultreon.quantum.client.api.events.RenderScreenEvent;
import dev.ultreon.quantum.client.api.events.RenderWorldEvent;
import dev.ultreon.quantum.client.config.ClientConfiguration;
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
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.debug.profiler.ProfilerSection;
import dev.ultreon.quantum.debug.timing.Timing;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.ultreon.quantum.client.QuantumClient.LOGGER;
import static dev.ultreon.quantum.client.QuantumClient.PROFILER;
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
    private final float[] mouseVec = new float[2];
    private final Vector3 tmpV3 = new Vector3();
    private final Vec3f tmp3F1 = new Vec3f();
    private final Vec3f tmp3F2 = new Vec3f();
    private final Vec3d tmp3D1 = new Vec3d();
    private final Vec3d tmp3D2 = new Vec3d();
    private final Vec3d pos = new Vec3d();

    /**
     * Constructs a new GameRenderer with the specified client, model batch, and render pipeline.
     *
     * @param client     the client instance to be used by the renderer
     */
    public GameRenderer(QuantumClient client) {
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
        ClientWorld world = this.client.world;
        WorldRenderer worldRenderer = this.client.worldRenderer;

        LocalPlayer player = this.client.player;
        if (!GamePlatform.get().hasBackPanelRemoved()) {
            renderer.clearColor(0, 0, 0, 1);
        }

        if (player != null) {
            try (ProfilerSection ignored1 = QuantumClient.PROFILER.start("camera")) {
                positionCamera(deltaTime, player);
            }
        }

        client.backgroundCat.update(deltaTime);
        client.mainCat.update(deltaTime);
        client.worldCat.update(deltaTime);

        renderWorld(deltaTime, world, worldRenderer);

        renderer.begin();

        Screen screen = this.client.screen;


        renderOverlays(renderer, deltaTime, world, worldRenderer, screen);

        renderer.end();
    }

    private void renderOverlays(Renderer renderer, float deltaTime, ClientWorld world, WorldRenderer worldRenderer, Screen screen) {
        renderer.pushMatrix();
        renderer.translate(this.client.getDrawOffset().x, this.client.getDrawOffset().y);
        renderer.scale(this.client.getGuiScale(), this.client.getGuiScale());
        try (ProfilerSection ignored = QuantumClient.PROFILER.start("overlay")) {
            if (!GamePlatform.get().hasBackPanelRemoved() && !(this.client.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed())) {
                renderer.clearColor(1 / 255f, 1 / 255f, 1 / 255f, 1);
            }

            if (GamePlatform.get().isDevFlagEnabled(DevFlag.OcclusionDebug)) {
                WorldRenderer worldRenderer1 = this.client.worldRenderer;
                if (worldRenderer1 != null && !worldRenderer1.isDisposed()) {
                    for (Rectangle occlusionBound : worldRenderer1.occlusionBounds) {
                        renderer.box((int) occlusionBound.x * (int) client.getGuiScale(), (int) occlusionBound.y * (int) client.getGuiScale(), (int) occlusionBound.width * (int) client.getGuiScale(), (int) occlusionBound.height * (int) client.getGuiScale(), Color.WHITE);
                    }
                }
            }

            this.renderOverlays(renderer, screen, world, deltaTime);

            if (this.client.crashOverlay != null) {
                if (!GamePlatform.get().isMacOSX() && Gdx.input.isKeyPressed(Input.Keys.F1) && Gdx.input.isKeyPressed(Input.Keys.C)) {
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
            try (ProfilerSection ignored = QuantumClient.PROFILER.start("world")) {
                EventSystem.postDefault(new RenderWorldEvent.Pre(world, worldRenderer, deltaTime));

                float blurScale = this.blurScale;
                blurScale += client.screen != null ? Gdx.graphics.getDeltaTime() * 3f : -Gdx.graphics.getDeltaTime() * 3f;

                blurScale = Mth.clamp(blurScale, 0f, 1f);
                this.blurScale = blurScale;

                this.renderWorld(deltaTime);
                EventSystem.postDefault(new RenderWorldEvent.Post(world, worldRenderer, deltaTime));
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
                    float dx = -Gdx.input.getDeltaX() * ClientConfiguration.cameraSensitivity.getValue();
                    float dy = -Gdx.input.getDeltaY() * ClientConfiguration.cameraSensitivity.getValue();
                    player.rotateHead(dx, dy);
                }
            } else if (GamePlatform.get().isWeb()) {
                float dx = -Gdx.input.getDeltaX() * ClientConfiguration.cameraSensitivity.getValue();
                float dy = -Gdx.input.getDeltaY() * ClientConfiguration.cameraSensitivity.getValue();
                player.rotateHead(dx, dy);
            } else if (GamePlatform.get().isDesktop()) {
                float dx = -(Gdx.input.getX() - centerX) * ClientConfiguration.cameraSensitivity.getValue();
                float dy = -(Gdx.input.getY() - centerY) * ClientConfiguration.cameraSensitivity.getValue();
                player.rotateHead(dx, dy);
            }

            // Reset position
            Gdx.input.setCursorPosition(centerX, centerY);
        }

        if (client.detachedCam) {
            client.renderCamera.position.set(client.detachedPos);

            Vector2 rotation = this.tmp.set(client.detachedRot.x, client.detachedRot.y);
            Quaternion quaternion = new Quaternion();
            quaternion.setFromAxis(Vector3.Y, rotation.x);
            quaternion.mul(new Quaternion(Vector3.X, rotation.y));
            quaternion.conjugate();

            // Calculate the direction vector
            float yRot = client.detachedRot.y;
            float xHeadRot = client.detachedRot.x;
            tmp3F2.x = (float) (Math.cos(Math.toRadians(yRot)) * Math.sin(Math.toRadians(xHeadRot)));
            tmp3F2.z = (float) (Math.cos(Math.toRadians(yRot)) * Math.cos(Math.toRadians(xHeadRot)));
            tmp3F2.y = (float) (Math.sin(Math.toRadians(yRot)));

            // Normalize the direction vector
            tmp3F2.nor();
            this.client.renderCamera.direction.set(tmp3F2.x, tmp3F2.y, tmp3F2.z);

            // Add camera bop. Use easing and animate with cameraBop. Camera Bop is a sort of camera movement while walking.
            float cameraBop = calculateCameraBop(deltaTime);

            this.client.renderCamera.up.set(0, 1, 0);
            this.client.renderCamera.up.rotate(Vector3.Y, rotation.x);
            this.client.renderCamera.up.rotate(Vector3.Z, cameraBop);
            this.client.renderCamera.up.rotate(Vector3.Y, -rotation.x);
            return;
        }

        this.client.camera.update(player);
        this.client.camera.far = ((float) ClientConfiguration.renderDistance.getValue() / CS - 1) * World.CS / WorldRenderer.SCALE;

        Vector2 rotation = this.tmp.set(player.xHeadRot, player.yRot);
        Quaternion quaternion = new Quaternion();
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
     *
     * @param deltaTime The time elapsed since the last frame.
     */
    void renderWorld(float deltaTime) {
        PROFILER.begin("game-renderer@render-world");
        try {
            if (renderWorldBuffers(deltaTime)) return;

            // Cursor
            renderCursor();
        } finally {
            PROFILER.end();
        }
    }

    private void renderCursor() {
        PROFILER.begin("game-renderer@render-cursor");
        try {
            if (client.cursor instanceof BlockHit) {
                BlockHit blockHit = (BlockHit) client.cursor;

                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                Gdx.gl.glDepthMask(false);
                drawBlockOutline(client.shapeRenderer, client.camera.relative(blockHit.getBlockVec().d(), tmpV3), 1f, 3.5f);
                Gdx.gl.glDepthMask(true);
            }
        } finally {
            PROFILER.end();
        }
    }

    private boolean renderWorldBuffers(float deltaTime) {
        RenderBufferSource bufferSource = this.client.renderBuffers();
        PROFILER.begin("game-renderer@render-world-buffers");
        try {
            bufferSource.begin(this.client.camera);

            // Background
            @Nullable ClientWorldAccess world = this.client.world;
            @Nullable TerrainRenderer worldRenderer = this.client.worldRenderer;
            LocalPlayer localPlayer = this.client.player;

            // World
            if (localPlayer == null || worldRenderer == null || world == null) {
                LOGGER.warn("worldRenderer or localPlayer is null");
                return true;
            }
            if (this.client.renderWorld) {
                worldRenderer.renderBackground(bufferSource, Gdx.graphics.getDeltaTime());
            }

            bufferSource.getBuffer(RenderPass.SKYBOX).flush();
            bufferSource.getBuffer(RenderPass.CELESTIAL_BODIES).flush();

            Vec3d position = localPlayer.getPosition(client.partialTick);
            Array<Entity> toSort = new Array<>(world.getAllEntities());
            worldRenderer.render(client.renderBuffers(), deltaTime);
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
        } finally {
            PROFILER.end();
        }
        return false;
    }

    public void drawBlockOutline(ShapeRenderer renderer, Vector3 relativeBlockPos, float size, float lineWidth) {
        // Slightly inflate the cube to avoid z-fighting
        PROFILER.begin("game-renderer@draw-block-outline");
        try {
            float inflate = 0.002f;

            // Set line width (constant pixel size)
            Gdx.gl.glLineWidth(lineWidth);

            renderer.setProjectionMatrix(client.camera.combined);
            renderer.begin(ShapeRenderer.ShapeType.Line);
            renderer.setColor(0f, 0f, 0f, 0.2f);

            renderer.box(relativeBlockPos.x - inflate, relativeBlockPos.y - inflate, relativeBlockPos.z + 1 - inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);
            renderer.box(relativeBlockPos.x + inflate, relativeBlockPos.y - inflate, relativeBlockPos.z + 1 - inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);
            renderer.box(relativeBlockPos.x - inflate, relativeBlockPos.y + inflate, relativeBlockPos.z + 1 - inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);
            renderer.box(relativeBlockPos.x + inflate, relativeBlockPos.y + inflate, relativeBlockPos.z + 1 - inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);
            renderer.box(relativeBlockPos.x - inflate, relativeBlockPos.y - inflate, relativeBlockPos.z + 1 + inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);
            renderer.box(relativeBlockPos.x + inflate, relativeBlockPos.y - inflate, relativeBlockPos.z + 1 + inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);
            renderer.box(relativeBlockPos.x - inflate, relativeBlockPos.y + inflate, relativeBlockPos.z + 1 + inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);
            renderer.box(relativeBlockPos.x + inflate, relativeBlockPos.y + inflate, relativeBlockPos.z + 1 + inflate, size + inflate * 2, size + inflate * 2, size + inflate * 2);

            renderer.end();

            // Reset line width
            Gdx.gl.glLineWidth(1f);
        } finally {
            PROFILER.end();
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

        try (ProfilerSection ignored = QuantumClient.PROFILER.start("screen")) {
            GridPoint2 mouseOffset = this.client.getMousePos();
            mouseVec[0] = GamePlatform.get().isShowingImGui() ? mouseOffset.x / this.client.getGuiScale() : Gdx.input.getX() / this.client.getGuiScale();
            mouseVec[1] = GamePlatform.get().isShowingImGui() ? mouseOffset.y / this.client.getGuiScale() : Gdx.input.getY() / this.client.getGuiScale();

            processMouseCoords(mouseVec);

            EventSystem.postDefault(new RenderScreenEvent.Pre(screen, renderer, mouseVec[0], mouseVec[1], deltaTime));

            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            renderer.getBatch().enableBlending();
            renderer.getBatch().setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);

            Timing.start("screen");
            screen.render(renderer, deltaTime);
            screen.renderTooltips(renderer, (int) mouseVec[0], (int) mouseVec[1], deltaTime);
            Timing.end("screen");

            EventSystem.postDefault(new RenderScreenEvent.Post(screen, renderer, mouseVec[0], mouseVec[1], deltaTime));

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
    }

    private void renderHUD(Renderer renderer, ClientWorldAccess world, float deltaTime) {
        if (world != null) {
            try (ProfilerSection ignored = QuantumClient.PROFILER.start("hud")) {
                if (EventSystem.postCancelable(new RenderHudEvent.Pre(world, renderer, deltaTime))) return;
                OverlayManager.render(renderer, deltaTime);
                EventSystem.postDefault(new RenderHudEvent.Post(world, renderer, deltaTime));
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

        if (vignetteTex != null) vignetteTex.dispose();
    }
}
