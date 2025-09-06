package dev.ultreon.quantum.client.render.modes.basic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.ClientPlatform;
import dev.ultreon.quantum.client.management.TextureAtlasManager;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.client.render.context.RenderContext;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.client.render.world.ChunkRenderState;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.graphics.VertexAttribute.*;

public class BasicRenderPass extends RenderPass {
    private @Nullable RenderType skyBox;
    private @Nullable RenderType celestialBodies;
    private @Nullable RenderType transparent;
    private @Nullable RenderType water;
    private @Nullable RenderType opaque;
    private @Nullable RenderType gizmo;
    private @Nullable RenderType gizmoOutline;
    private @Nullable RenderType entityTransparent;
    private @Nullable RenderType cutout;
    private @Nullable FrameBuffer frameBuffer;

    private final BasicShaders shaderHolder = new BasicShaders();
    private Texture[] textures;
    private boolean enabled;

    public BasicRenderPass() {
        super("basic");
    }

    @Override
    protected void resize(int newWidth, int newHeight) {
        
    }

    @Override
    protected void create() {
        if (enabled) return;
        enabled = true;

        this.shaderHolder.enable();

        this.skyBox = RenderType.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
                .name("skybox")
                .shader(() -> shaderHolder.skyBox)
                .depthTest(false)
                .build();

        this.celestialBodies = RenderType.builder(Position(), TexCoords(0))
                .name("celestial_bodies")
                .shader(() -> shaderHolder.celestialBodies)
                .blending(GL_SRC_ALPHA, GL_ONE)
                .cull(0)
                .depthTest(false)
                .build();

        this.transparent = RenderType.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
                .name("transparent")
                .shader(() -> shaderHolder.transparent)
                .blending()
                .depthTest()
                .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
                .build();

        this.water = RenderType.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
                .name("water")
                .shader(() -> shaderHolder.water)
                .blending()
                .depthTest()
                .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
                .build();

        this.opaque = RenderType.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
                .name("opaque")
                .shader(() -> shaderHolder.opaque)
                .depthTest()
                .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
                .build();

        this.gizmo = RenderType.builder(Position(), ColorPacked(), TexCoords(0))
                .name("gizmo")
                .shader(() -> shaderHolder.gizmo)
                .alphaTest(0.01f)
                .blending()
                .depthTest()
                .build();

        this.gizmoOutline = RenderType.builder(Position(), ColorPacked(), TexCoords(0))
                .name("gizmo_outline")
                .shader(() -> shaderHolder.gizmoOutline)
                .alphaTest(0.01f)
                .blending()
                .depthTest()
                .build();

        this.entityTransparent = RenderType.builder(Position(), Normal(), TexCoords(0))
                .name("entity_transparent")
                .shader(() -> shaderHolder.entityTransparent)
                .blending()
                .depthTest()
                .build();

        this.cutout = RenderType.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
                .name("cutout")
                .shader(() -> shaderHolder.cutout)
                .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
                .alphaTest()
                .depthTest()
                .build();
        
        this.frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        this.textures = new Texture[]{frameBuffer.getColorBufferTexture()};
    }

    @Override
    public void dispose() {
        if (!enabled) return;
        enabled = false;

        this.shaderHolder.disable();
        this.skyBox = null;
        this.celestialBodies = null;
        this.transparent = null;
        this.water = null;
        this.opaque = null;
        this.gizmo = null;
        this.gizmoOutline = null;
        this.entityTransparent = null;
        this.cutout = null;
    }

    @Override
    public void render(RenderBufferSource bufferSource, RenderContext context) {
        if (!enabled) throw notEnabled();

        FrameBuffer frameBuffer = this.frameBuffer;
        if (frameBuffer == null) throw notEnabled();

        frameBuffer.begin();

        ScreenUtils.clear(0, 0, 0, 0, true);

        Gdx.gl.glDepthMask(false);

        bufferSource.begin(context.client.camera);

        renderSkyBox(bufferSource, context);

        Gdx.gl.glDepthMask(true);

        // Check if the world is disposed.
        LocalPlayer player = context.client.player;
        if (updateWorld(context, player)) return;

        // Get the loaded chunks and sort them by distance from the player.
        List<ClientChunk> chunks = prepareChunks(context, player);

        // Create a new ChunkRenderRef and an array of ChunkVec.
        ChunkRenderState ref = new ChunkRenderState();

        initBuffers(bufferSource);

        // Collect the chunks to render.
        context.renderTerrain(bufferSource, chunks, player, ref);
        context.renderGizmos(Gdx.graphics.getDeltaTime());

        renderEntities(bufferSource, context, player);

        // Particles
        renderParticles(context);

        bufferSource.end();

        context.pushInfo();
        frameBuffer.begin();
    }

    private @NotNull List<ClientChunk> prepareChunks(RenderContext context, LocalPlayer player) {
        List<ClientChunk> chunks = context.chunksInViewSorted(getRayVisibleChunks(context, context.world), player);
        context.loadedChunks = chunks.size();
        context.visibleChunks = 0;
        return chunks;
    }

    private void renderEntities(RenderBufferSource bufferSource, RenderContext context, @NotNull LocalPlayer player) {
        context.renderEntity(bufferSource, this, player);
        Array<Entity> toSort = new Array<>(context.world.getAllEntities());
        for (Entity entity : toSort.toArray(Entity.class)) {
            if (entity instanceof LocalPlayer) continue;
            context.renderEntity(bufferSource, this, entity);
        }

        toSort.clear();
    }

    private void renderParticles(RenderContext context) {
        ParticleSystem particleSystem = context.worldRenderer.getParticleSystem();
        if (particleSystem != null) {
            particleSystem.begin();
            particleSystem.updateAndDraw(Gdx.graphics.getDeltaTime());
            particleSystem.end();
//        modelBatch.render(particleSystem);
            // TODO add particle system
        }
    }

    private void initBuffers(RenderBufferSource bufferSource) {
        bufferSource.getBuffer(RenderType.OPAQUE);
        bufferSource.getBuffer(RenderType.WATER);
        bufferSource.getBuffer(RenderType.TRANSPARENT);
        bufferSource.getBuffer(RenderType.CUTOUT);
    }

    @Contract("_, null -> true; _, !null -> _")
    private boolean updateWorld(RenderContext context, LocalPlayer player) {
        if (player == null) return true;
        if (context.worldRenderer.isDisposed()) return true;

        // Update the skybox and environment.
        context.skybox.update(context.world.getDaytime());
        context.fogColor.set(context.skybox.bottomColor);
        return false;
    }

    private void renderSkyBox(RenderBufferSource bufferSource, RenderContext context) {
        bufferSource.getBuffer(RenderType.SKYBOX).begin(context.client.camera);
        context.skybox.render(bufferSource, this);
        context.fogColor.set(context.skybox.bottomColor);
        bufferSource.getBuffer(RenderType.SKYBOX).end();

        bufferSource.getBuffer(RenderType.SKYBOX).flush();
        bufferSource.getBuffer(RenderType.CELESTIAL_BODIES).flush();
    }

    @Override
    public @Nullable RenderType renderTypeFor(RenderMaterial renderMaterial) {
        if (renderMaterial == null) return null;

        switch (renderMaterial.getObjectType()) {
            case SKYBOX:
                return this.skyBox;
            case CELESTIAL_BODIES:
                return this.celestialBodies;
            case SOLID_BLOCK:
                return this.opaque;
            case TRANSPARENT_BLOCK:
                return this.transparent;
            case CUTOUT_BLOCK:
            case ENTITY_ITEM:
            case ITEM:
                return this.cutout;
            case ENTITY:
                return this.entityTransparent;
            case LAVA:
            case WATER:
            case CUSTOM_LIQUID:
                return this.water;
        }

        return null;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public Texture[] getTextures() {
        return textures;
    }
}
