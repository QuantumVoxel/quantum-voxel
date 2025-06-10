package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.DisposableContainer;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.gui.screens.world.WorldLoadScreen;
import dev.ultreon.quantum.client.management.MaterialManager;
import dev.ultreon.quantum.client.model.EntityModelInstance;
import dev.ultreon.quantum.client.model.QVModel;
import dev.ultreon.quantum.client.model.WorldRenderContextImpl;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.model.entity.renderer.EntityRenderer;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.registry.BlockRenderPassRegistry;
import dev.ultreon.quantum.client.render.*;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.*;
import static dev.ultreon.quantum.client.QuantumClient.*;
import static dev.ultreon.quantum.world.World.CS;

/**
 * The {@code WorldRenderer} class is responsible for rendering the game world based on the client-side
 * world state. It handles the rendering of chunks, entities, particles, dynamic lighting,
 * skyboxes, and special effects such as block breaking animations.
 *
 * This class provides functionality for managing rendering contexts, creating and disposing
 * of rendering resources, and updating the visual state of the world.
 */
@SuppressWarnings("GDXJavaUnsafeIterator")
public final class WorldRenderer implements DisposableContainer, TerrainRenderer {
    public static final float SCALE = 1;
    private static final Vec3d TMP_3D_A = new Vec3d();
    private static final Vec3d TMp_3D_B = new Vec3d();
    public static final String OUTLINE_CURSOR_ID = CommonConstants.strId("outline_cursor");
    public static final int QV_CHUNK_ATTRS = VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal;
    public static final NamespaceID MOON_ID = NamespaceID.of("generated/moon");
    public static final NamespaceID SUN_ID = NamespaceID.of("generated/sun");
    public ParticleSystem particleSystem = new ParticleSystem();
    final AsyncExecutor executor = new AsyncExecutor(Math.max(GamePlatform.get().cpuCores() / 3, 4));

    private Material material;
    private Material transparentMaterial;
    private Texture breakingTex;
    private Environment environment;
    private int visibleChunks;
    private int loadedChunks;
    static final Vector3 CHUNK_DIMENSIONS = new Vector3(CS, CS, CS);
    static final Vector3 HALF_CHUNK_DIMENSIONS = WorldRenderer.CHUNK_DIMENSIONS.cpy().scl(0.5f);

    private ClientWorld world;
    private final QuantumClient client = QuantumClient.get();

    @Nullable
    private ModelInstance cursor = null;
    @Nullable
    private CelestialBody sun = null;
    @Nullable
    private CelestialBody moon = null;
    @Nullable
    private BlockHit lastHitResult;

    private boolean disposed = false;
    private final Vector3 tmp = new Vector3();
    private final Array<Model> breakingModels = new Array<>();
    private final Int2ObjectMap<ClientEntityInfo> modelInstances = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<QVModel> qvModels = new Int2ObjectOpenHashMap<>();
    private final List<Disposable> disposables = new ArrayList<>();
    private final Skybox skybox = new Skybox();
    private final Map<BlockVec, ModelInstance> breakingInstances = new HashMap<>();
    private final Map<BlockVec, ModelInstance> blockInstances = new ConcurrentHashMap<>();
    private final Map<ChunkVec, ChunkModel> chunkModels = new ConcurrentHashMap<>();
    private boolean wasSunMoonShown = true;
    private final Vector3 sunDirection = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final BlendingAttribute attribute = new BlendingAttribute(0.5f);
    private @Nullable Vector3 lastCamPos;
    private @Nullable Vector3 lastCamDir;
    private @Nullable Vec3d lastPlayerPos;
    private final Array<RenderBuffer> buffers = new Array<>(RenderBuffer.class);
    private final Color fogColor = new Color(0.6F, 0.7F, 1.0F, 1.0F);

    /**
     * Constructs a WorldRenderer instance for rendering a given client world.
     *
     * @param world the client world to be rendered, must not be null
     */
    public WorldRenderer(@NotNull ClientWorld world) {
        this.world = world;
        this.setup();
        world.add("Skybox", skybox);
    }

    /**
     * Initializes various graphical components and environment settings for the client.
     */
    private void setup() {
        Texture blockTex = this.client.blocksTextureAtlas.getTexture();
        Texture emissiveBlockTex = this.client.blocksTextureAtlas.getEmissiveTexture();

        this.setupMaterials(blockTex, emissiveBlockTex);
        this.setupDynamicSkybox();
        this.setupSunAndMoon();
        this.setupBreaking();
        this.setupEnvironment();
        this.setupParticles();
    }

    private void setupParticles() {
//        BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
//        billboardParticleBatch.setCamera(this.client.camera);
//        this.particleSystem.add(billboardParticleBatch);
    }

    private void setupEnvironment() {
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
        this.environment.set(new ColorAttribute(ColorAttribute.Fog, 0.6F, 0.7F, 1.0F, 1.0F));
        this.environment.set(new ColorAttribute(ColorAttribute.Specular, 1, 1, 1, 1f));
    }

    private void setupBreaking() {
        // Breaking animation meshes.
        this.setBreakingTex(this.client.getTextureManager().getTexture(NamespaceID.of("textures/break_stages.png")));

        Array<TextureRegion> breakingTexRegions = new Array<>(new TextureRegion[6]);
        for (int i = 0; i < 6; i++) {
            TextureRegion textureRegion = new TextureRegion(this.getBreakingTex(), 0, i / 6f, 1, (i + 1) / 6f);
            breakingTexRegions.set(i, textureRegion);
        }

        var boundingBox = Blocks.STONE.getBoundingBox(0, 0, 0, Blocks.STONE.getDefaultState());
        float v = 0.001f;
        boundingBox.set(boundingBox);
        boundingBox.min.sub(v);
        boundingBox.max.add(v);

        for (int i = 0; i < 6; i++) {
            BakedCubeModel bakedCubeModel = BakedCubeModel.of(new NamespaceID("break_stage/stub_" + i), breakingTexRegions.get(i), "transparent");
            Model model = bakedCubeModel.getModel();

            this.breakingModels.add(model);
        }
    }

    /**
     * Configures and initializes a dynamic skybox for the scene.
     * This method creates a cube model with specific material properties and attributes,
     * representing a skybox that dynamically integrates with the rendering pipeline.
     */
    private void setupDynamicSkybox() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material();
        material.id = NamespaceID.of("generated/skybox_material").toString();
        material.set(ColorAttribute.createDiffuse(0, 1, 0, 1));
        material.set(new BlendingAttribute());
        material.set(new DepthTestAttribute(GL_LEQUAL, true));
        material.set(IntAttribute.createCullFace(0));

        this.skybox.model = modelBuilder.createBox(60, 60, 60, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked);

        ModelInstance modelInstance = new ModelInstance(this.skybox.model, 0, 0, 0);
        modelInstance.userData = Shaders.SKYBOX.get();
        this.skybox.modelInstance = modelInstance;
    }

    /**
     * Initializes and sets up the materials used in the rendering process, including
     * both opaque and transparent materials with appropriate texture attributes, blending,
     * and depth testing settings.
     *
     * @param blockTex the texture used for the diffuse property of the materials
     * @param emissiveBlockTex the texture used for the emissive property of the materials
     */
    private void setupMaterials(Texture blockTex, Texture emissiveBlockTex) {
        this.material = new Material();
        this.material.set(TextureAttribute.createDiffuse(blockTex));
        this.material.set(TextureAttribute.createEmissive(emissiveBlockTex));
        this.material.set(new DepthTestAttribute(GL_LEQUAL));
        this.transparentMaterial = new Material();
        this.transparentMaterial.set(TextureAttribute.createDiffuse(blockTex));
        this.transparentMaterial.set(TextureAttribute.createEmissive(emissiveBlockTex));
        this.transparentMaterial.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
        this.transparentMaterial.set(new DepthTestAttribute(GL_LEQUAL));
        this.transparentMaterial.set(FloatAttribute.createAlphaTest(0.01f));
    }

    /**
     * Sets up the sun and moon models with the necessary materials and textures, and creates their respective ModelInstances.
     */
    private void setupSunAndMoon() {
        // Create the sun model with sun material and texture
        Model sunModel = ModelManager.INSTANCE.generateModel(SUN_ID, modelBuilder -> {
            Material sunMat = new Material();
            sunMat.id = NamespaceID.of("generated/sun_material").toString();

            modelBuilder.part(NamespaceID.of("generated/sun_part").toString(), createSun(), GL_TRIANGLES, sunMat);
        });

        // Create the moon model with moon material and texture
        Model moonModel = ModelManager.INSTANCE.generateModel(MOON_ID, modelBuilder -> {
            Material moonMat = new Material();
            moonMat.id = NamespaceID.of("generated/moon_material").toString();
            moonMat.set(TextureAttribute.createDiffuse(this.client.getTextureManager().getTexture(NamespaceID.of("textures/environment/moon.png"))));
            moonMat.set(new DepthTestAttribute(GL_LEQUAL, true));
            moonMat.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE));
            moonMat.set(IntAttribute.createCullFace(0));
            moonMat.set(FogAttribute.createFog(1, 1, 1));

            modelBuilder.part(NamespaceID.of("generated/moon_part").toString(), createMoon(), GL_TRIANGLES, moonMat);
        });

        // Create ModelInstances for sun and moon
        sun = new CelestialBody(sunModel);
        moon = new CelestialBody(moonModel);

        world.add("Sun", sun);
        world.add("Moon", moon);
    }

    /**
     * Creates a mesh representing the sun, with a triangular quad shape.
     *
     * @return the sun mesh
     */
    private Mesh createSun() {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);

        // Define the vertices of the quad, with their positions, normals, and UV coordinates
        meshBuilder.rect(new VertexInfo().setPos(-2, -2, 15).setNor(0, 0, -1).setUV(0, 0),  // Bottom left
                new VertexInfo().setPos(-2, 2, 15).setNor(0, 0, -1).setUV(0, 1),   // Top left
                new VertexInfo().setPos(2, 2, 15).setNor(0, 0, -1).setUV(1, 1),    // Top right
                new VertexInfo().setPos(2, -2, 15).setNor(0, 0, -1).setUV(1, 0)    // Bottom right
        );

        return meshBuilder.end();
    }

    /**
     * Creates a mesh representing the moon, with a triangular quad shape.
     *
     * @return the moon mesh
     */
    private Mesh createMoon() {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);

        // Define the vertices of the quad, with their positions, normals, and UV coordinates
        meshBuilder.rect(new VertexInfo().setPos(-2, -2, -15).setNor(0, 0, 1).setUV(0, 0),  // Bottom left
                new VertexInfo().setPos(-2, 2, -15).setNor(0, 0, 1).setUV(0, 1),   // Top left
                new VertexInfo().setPos(2, 2, -15).setNor(0, 0, 1).setUV(1, 1),    // Top right
                new VertexInfo().setPos(2, -2, -15).setNor(0, 0, 1).setUV(1, 0)    // Bottom right
        );

        return meshBuilder.end();
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    public static long getChunkMeshFrees() {
        return ValueTracker.getChunkMeshFrees();
    }

    public static long getVertexCount() {
        return ValueTracker.getVertexCount();
    }

    @Override
    public void free(ClientChunkAccess chunk) {
        if (!QuantumClient.isOnRenderThread()) {
            QuantumClient.invoke(() -> this.free(chunk));
            return;
        }

        if (!chunk.isInitialized()) return;

        chunk.revalidate();
        ValueTracker.setChunkMeshFrees(ValueTracker.getChunkMeshFrees() + 1);
    }

    @Override
    public @Nullable Entity removeEntity(int id) {
        this.checkThread();
        ClientEntityInfo remove = this.modelInstances.remove(id);
        if (remove == null) return null;
        client.worldCat.remove(remove);
        return null;
    }

    private void checkThread() {
        if (!QuantumClient.isOnRenderThread())
            throw new IllegalStateException("Should only be called on the main thread!");
    }

    /**
     * Renders the world to the screen using the provided ModelBatch and RenderLayer.
     *
     * @param bufferSource     the ModelBatch to render with
     * @param deltaTime the time between the last and current frame
     */
    @Override
    public void renderBackground(RenderBufferSource bufferSource, float deltaTime) {
        updateBackground();

        this.skybox.render0(bufferSource);

        CelestialBody sun = this.sun;
        if (sun != null) {
            bufferSource.getBuffer(RenderPass.CELESTIAL_BODIES).render(sun);
        }
        CelestialBody moon = this.moon;
        if (moon != null) {
            bufferSource.getBuffer(RenderPass.CELESTIAL_BODIES).render(moon);
        }
    }

    /**
     * Renders the world to the screen using the provided ModelBatch and RenderLayer.
     *
     * @param batch         the ModelBatch to render with
     * @param deltaTime     the time between the last and current frame
     */
    @Override
    public void render(RenderBufferSource batch, float deltaTime) {
        var player = this.client.player;
        if (player == null) return;
        if (this.disposed) return;

        // Update the skybox and environment.
        this.skybox.update(this.world.getDaytime());
        this.fogColor.set(this.skybox.bottomColor);

        // Get the loaded chunks and sort them by distance from the player.
        var chunks = WorldRenderer.chunksInViewSorted(this.world.getLoadedChunks(), player);
        this.loadedChunks = chunks.size();
        this.visibleChunks = 0;

        // Create a new ChunkRenderRef and an array of ChunkVec.
        var ref = new ChunkRenderRef();
        Array<ChunkVec> positions = new Array<>();

        batch.getBuffer(RenderPass.OPAQUE);
        batch.getBuffer(RenderPass.WATER);
        batch.getBuffer(RenderPass.TRANSPARENT);
        batch.getBuffer(RenderPass.CUTOUT);

        // Collect the chunks to render.
        try (var ignored = QuantumClient.PROFILER.start("chunks")) {
            this.collectChunks(batch, chunks, positions, player, ref);
        }

        // Render the cursor.
        @Nullable Hit gameCursor = null;
        if (this.client.cursor != null) gameCursor = this.client.cursor;
        if (gameCursor instanceof BlockHit && ((BlockHit) gameCursor).isCollide() && !this.client.hideHud && !player.isSpectator()) {
            BlockHit blockHit = (BlockHit) gameCursor;
            renderCursor(blockHit, player);
        }

        renderSelf(batch);
        renderGizmos(deltaTime);
    }

    private void renderCursor(BlockHit blockHit, LocalPlayer player) {
        try (var ignored = QuantumClient.PROFILER.start("cursor")) {
            // Block outline.
            Vec3i pos = blockHit.getBlockVec();
            Vec3f renderOffsetC = pos.d().sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f();
            var boundingBox = blockHit.getBlock().getBoundingBox(0, 0, 0, blockHit.getBlockMeta());
            renderOffsetC.add((float) boundingBox.min.x, (float) boundingBox.min.y, (float) boundingBox.min.z);

            // Render the outline.
            if (lastHitResult != null && this.lastHitResult.equals(blockHit)) {
                ModelManager.INSTANCE.unloadModel(NamespaceID.of("generated/selection_outline"));
                cursor = null;
            }

            this.lastHitResult = blockHit;

            if (this.cursor != null) ModelManager.INSTANCE.unloadModel(NamespaceID.of("generated/selection_outline"));

            Model model = ModelManager.INSTANCE.generateModel(NamespaceID.of("generated/selection_outline"), modelBuilder -> {
                Material material = new Material();
                material.id = NamespaceID.of("generated/selection_outline_material").toString();
                material.set(ColorAttribute.createDiffuse(1f, 1f, 1f, 1f));
                material.set(IntAttribute.createCullFace(GL_FRONT));


                var sizeX = (float) (boundingBox.max.x - boundingBox.min.x);
                var sizeY = (float) (boundingBox.max.y - boundingBox.min.y);
                var sizeZ = (float) (boundingBox.max.z - boundingBox.min.z);

                WorldRenderer.buildOutlineBox(sizeX + 0.1f, sizeY + 0.1f, sizeZ + 0.1f, modelBuilder.part("outline", GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked, material));
            });

            this.cursor = new ModelInstance(model, renderOffsetC.x - 0.05f, renderOffsetC.y - 0.05f, renderOffsetC.z - 0.05f);
            this.cursor.userData = Shaders.DEFAULT.get();
        }

        attribute.opacity = MathUtils.sinDeg((System.currentTimeMillis() % 360) / 1000f) / 90f + 0.5f;
        material.set(attribute);
    }

    private void renderSelf(RenderBufferSource batch) {
        drawSelf:
        try (var ignored = QuantumClient.PROFILER.start("(Local Player)")) {
            LocalPlayer localPlayer = this.client.player;
            if (localPlayer == null || !this.client.isInThirdPerson() && !ClientConfiguration.firstPersonPlayerModel.getValue()) {
                if (localPlayer != null) modelInstances.remove(localPlayer.getId());
                break drawSelf;
            }

            this.collectEntity(localPlayer, batch);
        }
    }

    private void renderGizmos(float deltaTime) {
        for (String category : world.getEnabledGizmoCategories()) {
            Gizmo[] gizmos = world.getGizmos(category);
            List<Gizmo> toSort = new ArrayList<>();
            for (Gizmo gizmo1 : gizmos) if (gizmo1 != null) toSort.add(gizmo1);
            toSort.sort((o1, o2) -> {
                double dst1 = 0;
                double dst2 = 0;
                if (client.player != null) {
                    dst1 = o1.position.dst(client.player.getPosition(deltaTime));
                    dst2 = o2.position.dst(client.player.getPosition(deltaTime));
                }
                return Double.compare(dst2, dst1);
            });
        }
    }

    public void renderForeground(RenderBufferSource batch, float deltaTime) {
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void setWorld(@Nullable ClientWorldAccess world) {
        if (world != null) this.world = (ClientWorld) world;
    }

    @SuppressWarnings("t")
    private void collectChunks(RenderBufferSource bufferSource, List<ClientChunk> chunks, Array<ChunkVec> positions, LocalPlayer player, ChunkRenderRef ref) {
        for (var chunk : chunks) {
            if (chunk.isEmpty()) continue;

            if (positions.contains(chunk.vec, false)) {
                QuantumClient.LOGGER.warn("Duplicate chunk: {}", chunk.vec);
                continue;
            }

            positions.add(chunk.vec);

            if (!chunk.isReady()) continue;
            if (chunk.isDisposed()) {
                unload(chunk);
                continue;
            }

            Vec3i chunkOffset = chunk.getOffset();
            Vec3f renderOffsetC = chunkOffset.d().sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f().div(WorldRenderer.SCALE);
            chunk.renderOffset.set(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z).add(chunk.deltaOffset);
            chunk.getBoundingBox().min.set(renderOffsetC.x - 16, renderOffsetC.y - 16, renderOffsetC.z - 16);
            chunk.getBoundingBox().max.set(renderOffsetC.x + 16, renderOffsetC.y + 16, renderOffsetC.z + 16);

            if (frustumCulling(chunk)) continue;

            ChunkModel model = this.chunkModels.get(chunk.vec);
            if (chunk.getWorld().isChunkInvalidated(chunk) || !chunk.initialized) {
                model = revalidateChunk(ref, chunk, model);
            } else if (model == null) {
                if (ref.chunkRendered || this.shouldIgnoreRebuild()) continue;
                model = buildChunk(ref, chunk);
            } else if (model.needsRebuild(world) && !(ref.chunkRendered || this.shouldIgnoreRebuild())) {
                rebuildChunk(ref, chunk, model);
                continue;
            }

            model.render(client.camera, bufferSource);

            this.renderBlockBreaking(bufferSource, chunk);

            model.setTranslation(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);

            this.visibleChunks++;
        }
    }

    private boolean frustumCulling(ClientChunk chunk) {
        try (var ignoredFrustumCullingSection = PROFILER.start("frustum-culling")) {
            if (updateCamPos()) return false;

            if (chunk.enabled && !this.client.camera.frustum.boundsInFrustum(chunk.getBoundingBox())) {
                chunk.enabled = false;
                return true;
            } else if (!chunk.enabled) {
                chunk.enabled = true;
            }
        }

        return !chunk.enabled;
    }

    private boolean updateCamPos() {
        if (lastCamPos != null && lastCamDir != null && lastPlayerPos != null) {
            if (client.camera.position.epsilonEquals(lastCamPos, 0.1f)) return true;
            if (client.camera.direction.epsilonEquals(lastCamDir, 0.1f)) return true;
            if (client.player != null && client.player.x - lastPlayerPos.x < 0.1f && client.player.y - lastPlayerPos.y < 0.1f && client.player.z - lastPlayerPos.z < 0.1f
                    && client.player.x - lastPlayerPos.x > -0.1f && client.player.y - lastPlayerPos.y > -0.1f && client.player.z - lastPlayerPos.z > -0.1f)
                return true;
            lastCamPos.set(client.camera.position);
            lastCamDir.set(client.camera.direction);
            if (client.player != null) lastPlayerPos.set(client.player.getPosition(0));
        } else {
            lastCamPos = new Vector3(client.camera.position);
            lastCamDir = new Vector3(client.camera.direction);
            if (client.player != null) lastPlayerPos = new Vec3d(client.player.getPosition(0));
        }
        return false;
    }

    /**
     * Builds a chunk model for the specified chunk using the provided render reference.
     * This method processes the chunk to create a new render model and updates its state
     * within the system.
     *
     * @param ref   the reference object that tracks information about the chunk rendering state
     * @param chunk the client chunk for which the model is to be built
     * @return the created chunk model
     */
    private @NotNull ChunkModel buildChunk(ChunkRenderRef ref, ClientChunk chunk) {
        ChunkModel model;
        try (var ignoredRebuildSection = this.client.profiler.start("build-chunk")) {
            chunk.dirty = false;
            model = new ChunkModel(chunk.vec, chunk, this);
            model.build();
            ref.chunkRendered = true;
            chunk.dirty = false;
            chunk.initialized = true;
            this.chunkModels.put(chunk.vec, model);
        }
        return model;
    }

    /**
     * Revalidates a chunk model by determining whether it needs to be rebuilt or reused based on
     * the provided rendering references and client chunk data. If the given model is null, it creates
     * a new chunk model. Otherwise, it checks the current rendering conditions to decide whether to
     * rebuild the chunk or reuse the existing model.
     *
     * @param ref   the reference object that tracks information about the chunk rendering state
     * @param chunk the client chunk containing the data for the chunk to be rendered
     * @param model the existing chunk model, or null if a new model needs to be created
     * @return the validated chunk model, either reused or newly built
     */
    private @NotNull ChunkModel revalidateChunk(ChunkRenderRef ref, ClientChunk chunk, @Nullable ChunkModel model) {
        if (model == null) {
            return buildChunk(ref, chunk);
        }
        if (!(client.screen instanceof WorldLoadScreen || ref.chunkRendered || this.shouldIgnoreRebuild())) {
            rebuildChunk(ref, chunk, model);
        }
        return model;
    }

    /**
     * Rebuilds the rendering model for a given chunk. This method processes the chunk, updates
     * its rendering state, and marks it as initialized and ready for rendering.
     *
     * @param ref   the reference object that tracks the chunk rendering state
     *              and indicates whether the chunk has been rendered
     * @param chunk the client chunk containing data that needs to be rendered
     * @param model the rendering model associated with the chunk,
     *              which will be rebuilt during this method
     */
    private void rebuildChunk(ChunkRenderRef ref, ClientChunk chunk, ChunkModel model) {
        try (var ignoredRebuildSection = this.client.profiler.start("rebuild")) {
            chunk.dirty = false;
            model.rebuild();
            ref.chunkRendered = true;
            chunk.dirty = false;

            chunk.onUpdated();
            chunk.initialized = true;
        }
    }

    /**
     * Unloads the specified client chunk from the world renderer. This method removes the
     * chunk's associated rendering model, ensures proper disposal of resources, and updates
     * internal tracking systems. It is designed to be thread-safe and will schedule execution
     * on the render thread if needed.
     *
     * @param chunk the client chunk to be unloaded
     */
    void unload(ClientChunk chunk) {
        if (!QuantumClient.isOnRenderThread()) {
            QuantumClient.invoke(() -> this.unload(chunk));
            return;
        }

        if (client.screen instanceof WorldLoadScreen)
            QuantumClient.LOGGER.warn("Chunk unloaded when loading game: ", new Throwable());

        ChunkModel chunkModel = chunkModels.remove(chunk.vec);
        if (chunkModel == null) {
            LOGGER.warn("Tried to unload a chunk that didn't exist: {}", chunk.vec);
            return;
        }
        client.worldCat.remove(chunkModel);
        if (chunkModel.getChunk() != chunk)
            throw new DeprecationCheckException("Model's chunk and chunk mismatch: " + chunk.vec);


        chunkModel.dispose();

//        Map<BlockVec, BlockState> customRendered = chunk.getCustomRendered();
//        for (var entry : blockInstances.entrySet()) {
//            if (customRendered.containsKey(entry.getKey())) {
//                ModelInstance value = entry.getValue();
//                client.worldCat.remove(value);
//                blockInstances.remove(entry.getKey());
//            }
//        }

        ValueTracker.setChunkMeshFrees(ValueTracker.getChunkMeshFrees() + 1);
    }

    /**
     * Renders the block models for a given client chunk using the specified buffer source.
     * This method processes the "custom rendered" blocks within the chunk and ensures that
     * block models are instantiated, transformed, and rendered per the current chunk state.
     * Existing models may be reused or removed based on the chunk's rendering data.
     *
     * @param bufferSource the buffer source used for rendering the block models
     * @param chunk        the client chunk that contains the blocks and associated rendering data
     */
    private void renderBlockModels(RenderBufferSource bufferSource, ClientChunk chunk) {
        for (var entry : chunk.getCustomRendered().entrySet())
            try (var ignored = this.client.profiler.start("render-block")) {
                BlockVec localVec = entry.getKey();
                Vector3 translation = this.tmp.set(chunk.renderOffset).add(localVec.getIntX(), localVec.getIntY(), localVec.getIntZ());

                BlockState blockState = entry.getValue();
                BlockModel blockModel = BlockModelRegistry.get().get(blockState);
                BlockVec globalVec = chunk.vec.blockInWorldSpace(localVec);
                if (!blockInstances.containsKey(globalVec) && blockModel != null) {
                    Model model = blockModel.getModel();
                    if (model != null) {
                        ModelInstance modelInstance = new ModelInstance(model, this.tmp);
                        this.blockInstances.put(globalVec, modelInstance);
                    }
                } else if (!chunk.getCustomRendered().containsKey(globalVec)) {
                    blockInstances.remove(globalVec);
                    continue;
                }

                ModelInstance modelInstance = blockInstances.get(globalVec);
                modelInstance.userData = Shaders.MODEL_VIEW.get();
                modelInstance.transform.setTranslation(translation);

                bufferSource.getBuffer(BlockRenderPassRegistry.get(blockState)).render(modelInstance);
            }
    }

    /**
     * Renders block breaking effects for a given client chunk. This method iterates through
     * the breaking blocks within the chunk, creates and transforms corresponding models,
     * and renders them using the provided buffer source.
     *
     * @param batch the buffer source used for rendering the breaking block models
     * @param chunk the client chunk that contains the blocks and their breaking states
     */
    private void renderBlockBreaking(RenderBufferSource batch, ClientChunk chunk) {
        for (var entry : chunk.getBreaking().entrySet()) {
            BlockVec pos = entry.getKey();

            Model breakingMesh = this.breakingModels.get(Math.round(Mth.clamp(1.0f * 5, 0, 5)));
            ModelInstance modelInstance = new ModelInstance(breakingMesh, this.tmp.x, this.tmp.y, this.tmp.z);
            Vector3 translation = this.tmp.set(chunk.renderOffset).add(pos.getIntX() + 1f, pos.getIntY(), pos.getIntZ());
            modelInstance.transform.setToTranslationAndScaling(translation, this.tmp2.set(1.1f, 1.1f, 1.1f));
            modelInstance.userData = Shaders.MODEL_VIEW.get();

            batch.getBuffer(RenderPass.TRANSPARENT).render(modelInstance);
        }
    }

    private boolean shouldIgnoreRebuild() {
        return false;
    }

    /**
     * Collects and prepares an entity for rendering, ensuring valid models and renderers are present,
     * and handles exceptions during the rendering process.
     *
     * @param entity The entity to be collected and rendered. It should have a valid ID and position
     *               within the acceptable rendering distance.
     * @param batch  The render buffer source used for rendering the entity. It provides access
     *               to the batch rendering context necessary for entity rendering.
     */
    @Override
    public void collectEntity(Entity entity, RenderBufferSource batch) {
        try {
            @Nullable QVModel model = this.qvModels.get(entity.getId());
            LocalPlayer player = QuantumClient.get().player;
            if (player == null || player.getPosition(client.partialTick).dst(entity.getPosition()) > 64 || entity instanceof Player && ((Player) entity).isSpectator()) {
                if (model != null) return;
                return;
            }

            //noinspection unchecked
            var renderer = (EntityRenderer<@NotNull Entity>) this.client.entityRendererManager.get(entity.getType());
            if (model == null) {
                if (renderer == null) {
                    QuantumClient.LOGGER.warn("Failed to render entity {} because it's renderer is null", entity.getId());
                    return;
                }
                model = renderer.createModel(entity);
                if (model == null) {
                    QuantumClient.LOGGER.warn("Failed to render entity {} because it's model instance is still null", entity.getId());
                    return;
                }
                this.modelInstances.put(entity.getId(), new ClientEntityInfo(model.getInstance()));
                this.qvModels.put(entity.getId(), model);
            }

            EntityModelInstance<@NotNull Entity> instance = new EntityModelInstance<>(model, entity);
            WorldRenderContextImpl<Entity> context = new WorldRenderContextImpl<>(batch, entity, entity.getWorld(), WorldRenderer.SCALE, player.getPosition(client.partialTick));

            model.update(Gdx.graphics.getDeltaTime());

            renderer.animate(instance, context);
            renderer.render(instance, context);

            Gizmo entityGizmo = world.getEntityGizmo(entity);
            if (entityGizmo != null)
                entityGizmo.position.set(entity.getPosition());
            Gdx.gl.glCullFace(GL_BACK);
        } catch (Exception e) {
            QuantumClient.LOGGER.error("Failed to render entity {}", entity.getId(), e);
            CrashLog crashLog = new CrashLog("Error rendering entity " + entity.getId(), new Exception());
            CrashCategory category = new CrashCategory("Entity", e);
            category.add("Entity ID", entity.getId());
            category.add("Entity Type", entity.getType().getId());
            crashLog.add("Entity", entity);
            crashLog.addCategory(category);
            crash(crashLog);
        }
    }

    public static void buildOutlineBox(float width, float height, float depth, MeshPartBuilder meshBuilder) {
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(0, 0, 0), new Vector3(width, height, depth)));
    }

    @NotNull
    private static List<ClientChunk> chunksInViewSorted(Collection<ClientChunk> chunks, Player player) {
        List<ClientChunk> list = new ArrayList<>(chunks);
        list = list.stream().sorted((o1, o2) -> {
            Vec3d mid1 = WorldRenderer.TMP_3D_A.set(o1.getOffset().x + (float) CS, o1.getOffset().y + (float) CS, o1.getOffset().z + (float) CS);
            Vec3d mid2 = WorldRenderer.TMp_3D_B.set(o2.getOffset().x + (float) CS, o2.getOffset().y + (float) CS, o2.getOffset().z + (float) CS);
            return Double.compare(mid1.dst(player.getPosition()), mid2.dst(player.getPosition()));
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public int getVisibleChunks() {
        return this.visibleChunks;
    }

    @Override
    public void reloadChunks() {
        for (var entry : List.copyOf(chunkModels.entrySet())) unload(entry.getValue().getChunk());
        this.chunkModels.clear();
        this.visibleChunks = 0;
        this.loadedChunks = 0;
    }

    @Override
    public int getLoadedChunksCount() {
        return this.loadedChunks;
    }

    public static long getPoolFree() {
        return ValueTracker.getPoolFree();
    }

    public static int getPoolPeak() {
        return ValueTracker.getPoolPeak();
    }

    public static int getPoolMax() {
        return ValueTracker.getPoolMax();
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    /**
     * Disposes all resources and performs cleanup for the {@code WorldRenderer}.
     * This includes shutting down executor services, releasing models, instances,
     * and disposables, as well as clearing all internal structures.
     *
     * Once this method is executed, the renderer is marked as disposed
     * and cannot be reused.
     *
     * Throws:
     * - {@link TerminationFailedException} if the executor service fails
     *   to terminate within the defined timeout period.
     */
    @Override
    public void dispose() {
        executor.dispose();

        this.disposed = true;

        Model skybox = this.skybox.model;
        this.skybox.model = null;
        this.skybox.modelInstance = null;
        if (skybox != null) skybox.dispose();

        ModelManager.INSTANCE.unloadModel(NamespaceID.of("generated/skybox"));

        for (var entry : chunkModels.entrySet()) {
            ClientChunk first = entry.getValue().getChunk();
            unload(first);
        }

        client.worldCat.clear();
        client.backgroundCat.clear();

        this.modelInstances.clear();
        this.blockInstances.clear();
        this.breakingInstances.clear();

        this.disposables.forEach(Disposable::dispose);
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    /**
     * Defers the disposal of the given disposable object within the {@code WorldRenderer} lifecycle.
     * If the {@code WorldRenderer} is already disposed, the provided disposable will be immediately disposed.
     * Otherwise, the disposable is added to the internal disposables list for future disposal when
     * the {@code WorldRenderer} itself is disposed.
     *
     * @param <T>         the type of the disposable, which must extend {@link Disposable}
     * @param disposable  the disposable object to defer for disposal; cannot be null
     * @return the same disposable object that was passed in
     * @throws NullPointerException if the provided disposable is null
     */
    @Override
    public <T extends Disposable> T deferDispose(T disposable) {
        if (this.disposables.contains(disposable)) return disposable;
        if (this.disposed) {
            QuantumClient.LOGGER.warn("World renderer already disposed, immediately disposing {}", disposable.getClass().getName());
            disposable.dispose();
            return disposable;
        }
        this.disposables.add(disposable);
        return disposable;
    }

    /**
     * Reloads the rendering context by resetting and reinitializing the necessary resources, models, and materials
     * for the rendering system. This method is responsible for handling textures, model instances, dynamic skyboxes,
     * breaking animations, environment setups, and particles.
     *
     * @param context the {@code ReloadContext} object that handles the submission of reload tasks
     * @param materialManager the {@code MaterialManager} responsible for managing material allocations and updates
     */
    @Override
    public void reload(ReloadContext context, MaterialManager materialManager) {
        context.submit(() -> {

            Texture blockTex = this.client.blocksTextureAtlas.getTexture();
            Texture emissiveBlockTex = this.client.blocksTextureAtlas.getEmissiveTexture();

            this.setupMaterials(blockTex, emissiveBlockTex);

            for (var entry : chunkModels.entrySet()) {
                ClientChunk first = entry.getValue().getChunk();
                unload(first);
            }

            client.backgroundCat.clear();

            ModelManager.INSTANCE.unloadModel(MOON_ID);
            ModelManager.INSTANCE.unloadModel(SUN_ID);

            this.modelInstances.clear();
            this.blockInstances.clear();

            this.setupMaterials(this.client.blocksTextureAtlas.getTexture(), this.client.blocksTextureAtlas.getEmissiveTexture());

            this.setupSunAndMoon();
            this.setupDynamicSkybox();
            this.setupBreaking();
            this.setupEnvironment();
            this.setupParticles();
        });
    }

    /**
     * Unloads the chunk identified by the specified ChunkVec if it exists in the world.
     *
     * @param chunkVec the chunk coordinates used to identify the chunk to be unloaded
     */
    private void unload(ChunkVec chunkVec) {
        ClientChunk clientChunk = this.world.getChunk(chunkVec);
        if (clientChunk != null) this.unload(clientChunk);
    }

    @Override
    public Skybox getSkybox() {
        return skybox;
    }

    /**
     * Updates the background to reflect the current configuration setting for the visibility
     * of the sun and moon. Depending on whether the display of the sun and moon is enabled
     * in the application configuration, this method adjusts their visibility accordingly.
     */
    @Override
    public void updateBackground() {
        if (ClientConfiguration.showSunAndMoon.getValue()) {
            updateSunMoon();
        } else if (wasSunMoonShown) {
            if (this.sun != null) this.sun.setVisible(false);
            if (this.moon != null) this.moon.setVisible(false);

            wasSunMoonShown = false;
        }
    }

    private void updateSunMoon() {
        if (!wasSunMoonShown) {
            if (this.sun != null) this.sun.setVisible(true);
            if (this.moon != null) this.moon.setVisible(true);
        }

        wasSunMoonShown = true;

        material.set(new DepthTestAttribute(GL_LEQUAL, true));
        var world = this.world;

        long daytime = world.getDaytime();

        // Sun angle
        float sunAngle = (float) ((daytime % 24000) / 24000.0 * Math.PI * 2);

        // Moon on the opposite side
        float moonAngle = (float) ((daytime % 24000) / 24000.0 * Math.PI * 2);

        if (this.sun != null)
            this.sun.transform.setToRotation(Vector3.Z, ClientWorld.SKYBOX_ROTATION.getDegrees()).rotate(Vector3.Y, sunAngle * MathUtils.radDeg - 180);
        if (this.moon != null)
            this.moon.transform.setToRotation(Vector3.Z, ClientWorld.SKYBOX_ROTATION.getDegrees()).rotate(Vector3.Y, moonAngle * MathUtils.radDeg - 180);

        this.sunDirection.setZero().setLength(1).rotate(Vector3.Z, ClientWorld.SKYBOX_ROTATION.getDegrees()).rotate(Vector3.Y, sunAngle * MathUtils.radDeg - 180);
    }

    @Override
    public void remove(ClientChunkAccess clientChunk) {
        this.unload(clientChunk.getVec());
    }

    /**
     * Adds particles to the particle system with the given position, motion, and count.
     *
     * @param obtained The particle effect to be added.
     * @param position The position where the particles should be spawned.
     * @param motion   The initial motion of the particles.
     * @param count    The number of particles to spawn.
     */
    @Override
    public void addParticles(ParticleEffect obtained, Vec3d position, Vec3d motion, int count) {
        LocalPlayer player = client.player;
        if (player == null) return;
        Vec3f div = position.sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f().div(WorldRenderer.SCALE);

        Vector3 vector3 = new Vector3(div.x, div.y, div.z);
        obtained.translate(vector3);
        particleSystem.add(obtained);
    }

    /**
     * Unloads a chunk from the client by removing its corresponding chunk model
     * and releasing any associated resources.
     *
     * @param clientChunk the client chunk access object that represents the chunk
     *                    to be unloaded
     */
    @Override
    public void unload(ClientChunkAccess clientChunk) {
        ChunkModel remove = this.chunkModels.remove(clientChunk.getVec());

        if (remove != null) remove.dispose();
    }

    @Override
    public @NotNull ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    @Nullable
    public Boolean rebuild(ClientChunk chunk) {
        if (!QuantumClient.isOnRenderThread()) {
            QuantumClient.invoke(() -> this.rebuild(chunk));
            return null;
        }

        ChunkModel model = this.chunkModels.get(chunk.vec);

        if (model != null) {
            model.rebuild();
            return true;
        }

        return false;
    }

    public Material getTransparentMaterial() {
        return transparentMaterial;
    }

    public void setTransparentMaterial(Material transparentMaterial) {
        this.transparentMaterial = transparentMaterial;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Texture getBreakingTex() {
        return breakingTex;
    }

    public void setBreakingTex(Texture breakingTex) {
        this.breakingTex = breakingTex;
    }

    public RenderBuffer[] buffers() {
        return this.buffers.toArray(RenderBuffer.class);
    }

    public Color getFogColor() {
        return fogColor;
    }

    private static class ChunkRenderRef {
        boolean chunkRendered = false;
    }
}
