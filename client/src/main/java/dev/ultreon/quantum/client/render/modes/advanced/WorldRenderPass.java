package dev.ultreon.quantum.client.render.modes.advanced;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import de.damios.guacamole.gdx.graphics.NestableFrameBuffer;
import dev.ultreon.quantum.client.management.TextureAtlasManager;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.registry.BlockRenderMaterial;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.client.render.context.RenderContext;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.render.gizmo.GizmoMaterial;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.client.render.world.ChunkRenderState;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_ONE;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.VertexAttribute.*;

public class WorldRenderPass extends RenderPass {
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

    private final AdvancedShaders shaderHolder = new AdvancedShaders();
    private boolean enabled;
    private Texture @Nullable [] textures;
    private NestableFrameBuffer skyBoxFrameBuffer;

    protected WorldRenderPass() {
        super("World (Advanced)");
    }

    @Override
    protected void resize(int newWidth, int newHeight) {
        if (frameBuffer != null) frameBuffer.dispose();
        frameBuffer = new NestableFrameBuffer.NestableFrameBufferBuilder(getWidth(), getHeight())
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                .addColorTextureAttachment(GL32.GL_RGBA16F, GL32.GL_RGBA, GL32.GL_FLOAT) // Normal
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888) // MRT
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888) // Roughness
                .addDepthTextureAttachment(GL32.GL_DEPTH32F_STENCIL8, GL32.GL_FLOAT)
                .build();
        
        if (skyBoxFrameBuffer != null) skyBoxFrameBuffer.dispose();
        skyBoxFrameBuffer = new NestableFrameBuffer(Pixmap.Format.RGB888, newWidth, newHeight, false);
    }

    @Override
    protected void create() {
        if (enabled) return;

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

        if (frameBuffer != null) frameBuffer.dispose();
        frameBuffer = new NestableFrameBuffer.NestableFrameBufferBuilder(getWidth(), getHeight())
                .addBasicColorTextureAttachment(Pixmap.Format.RGBA8888) // Color
                .addColorTextureAttachment(GL32.GL_RGBA16F, GL32.GL_RGBA, GL32.GL_FLOAT) // Normal
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888) // MRT
                .addBasicColorTextureAttachment(Pixmap.Format.RGB888) // Roughness
                .addDepthTextureAttachment(GL32.GL_DEPTH32F_STENCIL8, GL32.GL_FLOAT)
                .build();

        if (skyBoxFrameBuffer != null) skyBoxFrameBuffer.dispose();
        skyBoxFrameBuffer = new NestableFrameBuffer(Pixmap.Format.RGB888, getWidth(), getHeight(), false);

        assert frameBuffer != null;
        Array<Texture> textureAttachments = frameBuffer.getTextureAttachments();
        textures = new Texture[]{
                textureAttachments.get(0),
                textureAttachments.get(1),
                textureAttachments.get(2),
                textureAttachments.get(3),
                textureAttachments.get(4),
                skyBoxFrameBuffer.getColorBufferTexture()
        };
        
        this.enabled = true;
    }

    @Override
    public void dispose() {
        if (!enabled) return;
        if (frameBuffer != null) frameBuffer.dispose();
        if (skyBoxFrameBuffer != null) skyBoxFrameBuffer.dispose();
        shaderHolder.disable();
        enabled = false;
    }

    @Override
    public void render(RenderBufferSource bufferSource, RenderContext context) {
        if (!enabled) return;

        frameBuffer.begin();
        bufferSource.begin(context.client.camera);
        try {
            renderSkyBox(bufferSource, context);
            bufferSource.flush();

            // Check if the world is disposed.
            LocalPlayer player = context.client.player;
            if (updateWorld(context, player)) {
                return;
            }

            // Get the loaded chunks and sort them by distance from the player.
            List<ClientChunk> chunks = prepareChunks(context, player);

            // Create a new ChunkRenderRef and an array of ChunkVec.
            ChunkRenderState ref = new ChunkRenderState();

            initBuffers(bufferSource);

            // Collect the chunks to render.
            context.renderTerrain(bufferSource, chunks, player, ref, this);
            context.renderGizmos(Gdx.graphics.getDeltaTime());

            renderEntities(bufferSource, context, player);

            // Particles
            renderParticles(context);

            context.pushInfo();
        } finally {
            bufferSource.end();
            frameBuffer.end();
        }
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
        for (Entity entity : toSort.toArray(Entity[]::new)) {
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
        bufferSource.getBuffer(opaque);
        bufferSource.getBuffer(water);
        bufferSource.getBuffer(transparent);
        bufferSource.getBuffer(cutout);
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
        bufferSource.getBuffer(skyBox).begin(context.client.camera);
        context.skybox.render(bufferSource, this);
        context.fogColor.set(context.skybox.bottomColor);
        bufferSource.getBuffer(skyBox).end();

        bufferSource.getBuffer(skyBox).flush();
        bufferSource.getBuffer(celestialBodies).flush();
    }

    @Override
    public @Nullable RenderType renderTypeFor(RenderMaterial renderMaterial) {
        if (renderMaterial == null) return null;

        switch (renderMaterial.getObjectType()) {
            case SKYBOX:
                return this.skyBox;
            case CELESTIAL_BODIES:
                return this.celestialBodies;
            case BLOCK_OVERLAY:
                return this.transparent;
            case BLOCK:
                if (renderMaterial.getMaterialType() instanceof BlockRenderMaterial) {
                    BlockRenderMaterial material = (BlockRenderMaterial) renderMaterial.getMaterialType();
                    switch (material.getBlockRenderType()) {
                        case TRANSPARENT:
                            return this.transparent;
                        case SOLID:
                            return this.opaque;
                        case CUTOUT:
                            return this.cutout;
                    }
                }
                return this.opaque;
            case ENTITY_ITEM:
            case ITEM:
                return this.cutout;
            case ENTITY:
                return this.entityTransparent;
            case LAVA:
            case WATER:
            case CUSTOM_LIQUID:
                return this.water;
            case GIZMO:
                if (renderMaterial.getMaterialType() instanceof GizmoMaterial) {
                    GizmoMaterial gizmoMaterial = (GizmoMaterial) renderMaterial.getMaterialType();
                    if (gizmoMaterial.isOutline()) return this.gizmoOutline;
                }
                else return this.gizmo;
        }

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
