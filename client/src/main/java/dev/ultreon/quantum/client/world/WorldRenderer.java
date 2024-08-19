package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
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
import com.google.common.base.Preconditions;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.DisposableContainer;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.screens.WorldLoadScreen;
import dev.ultreon.quantum.client.management.MaterialManager;
import dev.ultreon.quantum.client.model.EntityModelInstance;
import dev.ultreon.quantum.client.model.QVModel;
import dev.ultreon.quantum.client.model.WorldRenderContextImpl;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.model.entity.renderer.EntityRenderer;
import dev.ultreon.quantum.client.multiplayer.MultiplayerData;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.render.shader.Shaders;
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
import static dev.ultreon.quantum.world.World.CHUNK_HEIGHT;
import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public final class WorldRenderer implements DisposableContainer, TerrainRenderer {
    public static final float SCALE = 1;
    private static final Vec3d TMP_3D_A = new Vec3d();
    private static final Vec3d TMp_3D_B = new Vec3d();
    public static final String OUTLINE_CURSOR_ID = CommonConstants.strId("outline_cursor");
    public static final int QV_CHUNK_ATTRS = VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal;
    public static final NamespaceID MOON_ID = id("generated/moon");
    public static final NamespaceID SUN_ID = id("generated/sun");
    public ParticleSystem particleSystem = new ParticleSystem();
    private Material material;
    private Material transparentMaterial;
    private Texture breakingTex;
    private Environment environment;
    private int visibleChunks;
    private int loadedChunks;
    private static final Vector3 CHUNK_DIMENSIONS = new Vector3(CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE);
    private static final Vector3 HALF_CHUNK_DIMENSIONS = WorldRenderer.CHUNK_DIMENSIONS.cpy().scl(0.5f);

    private final ClientWorld world;
    private final QuantumClient client = QuantumClient.get();

    private ModelInstance cursor = null;
    private ModelInstance sun = null;
    private ModelInstance moon = null;
    private boolean disposed = false;
    private final Vector3 tmp = new Vector3();
    private final Array<Model> breakingModels = new Array<>();
    private final Int2ObjectMap<ModelInstance> modelInstances = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<QVModel> qvModels = new Int2ObjectOpenHashMap<>();
    private final List<Disposable> disposables = new ArrayList<>();
    private long lastChunkBuild;
    private final Skybox skybox = new Skybox();
    private BlockHit lastHitResult;
    private final Map<BlockVec, ModelInstance> breakingInstances = new HashMap<>();
    private final Map<BlockVec, ModelInstance> blockInstances = new ConcurrentHashMap<>();
    private final Map<ChunkVec, ChunkModel> chunkModels = new ConcurrentHashMap<>();
    private boolean wasSunMoonShown = true;
    private final Vector3 sunDirection = new Vector3();
    private final Vector3 tmp2 = new Vector3();

    public WorldRenderer(@Nullable ClientWorld world) {
        this.world = world;

        this.setup();
    }

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
        BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
        billboardParticleBatch.setCamera(this.client.camera);
        this.particleSystem.add(billboardParticleBatch);
    }

    private void setupEnvironment() {
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
        this.environment.set(new ColorAttribute(ColorAttribute.Fog, 0.6F, 0.7F, 1.0F, 1.0F));
        this.environment.set(new ColorAttribute(ColorAttribute.Specular, 1, 1, 1, 1f));
    }

    private void setupBreaking() {
        // Breaking animation meshes.
        this.breakingTex = this.client.getTextureManager().getTexture(id("textures/break_stages.png"));

        Array<TextureRegion> breakingTexRegions = new Array<>(new TextureRegion[6]);
        for (int i = 0; i < 6; i++) {
            TextureRegion textureRegion = new TextureRegion(this.breakingTex, 0, i / 6f, 1, (i + 1) / 6f);
            breakingTexRegions.set(i, textureRegion);
        }

        var boundingBox = Blocks.STONE.getBoundingBox(0, 0, 0, Blocks.STONE.createMeta());
        float v = 0.001f;
        boundingBox.set(boundingBox);
        boundingBox.min.sub(v);
        boundingBox.max.add(v);

        for (int i = 0; i < 6; i++) {
            BakedCubeModel bakedCubeModel = BakedCubeModel.of(new NamespaceID("break_stage/stub_" + i), breakingTexRegions.get(i));
            Model model = bakedCubeModel.getModel();

            this.breakingModels.add(model);
        }
    }

    private void setupDynamicSkybox() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material();
        material.id = id("generated/skybox_material").toString();
        material.set(ColorAttribute.createDiffuse(0, 1, 0, 1));
        material.set(new BlendingAttribute());
        material.set(new DepthTestAttribute(GL_LEQUAL, true));
        material.set(IntAttribute.createCullFace(0));

        this.skybox.model = modelBuilder.createBox(60, 60, 60, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked);

        RenderLayer background = RenderLayer.BACKGROUND;
        ModelInstance modelInstance = background.create(this.skybox.model, 0, 0, 0);
        modelInstance.userData = Shaders.SKYBOX.get();
        this.skybox.modelInstance = modelInstance;
    }

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
            sunMat.id = id("generated/sun_material").toString();
            sunMat.set(TextureAttribute.createDiffuse(this.client.getTextureManager().getTexture(id("textures/environment/sun.png"))));
            sunMat.set(new DepthTestAttribute(GL_LEQUAL, true));
            sunMat.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE));
            sunMat.set(IntAttribute.createCullFace(0));
            sunMat.set(FogAttribute.createFog(1, 1, 1));

            modelBuilder.part(id("generated/sun_part").toString(), createSun(), GL_TRIANGLES, sunMat);
        });

        // Create the moon model with moon material and texture
        Model moonModel = ModelManager.INSTANCE.generateModel(MOON_ID, modelBuilder -> {
            Material moonMat = new Material();
            moonMat.id = id("generated/moon_material").toString();
            moonMat.set(TextureAttribute.createDiffuse(this.client.getTextureManager().getTexture(id("textures/environment/moon.png"))));
            moonMat.set(new DepthTestAttribute(GL_LEQUAL, true));
            moonMat.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE));
            moonMat.set(IntAttribute.createCullFace(0));
            moonMat.set(FogAttribute.createFog(1, 1, 1));

            modelBuilder.part(id("generated/moon_part").toString(), createMoon(), GL_TRIANGLES, moonMat);
        });

        // Create ModelInstances for sun and moon
        this.sun = RenderLayer.BACKGROUND.create(sunModel, 0, 0, 0);
        this.moon = RenderLayer.BACKGROUND.create(moonModel, 0, 0, 0);
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
        meshBuilder.rect(
                new VertexInfo().setPos(-2, -2, 15).setNor(0, 0, -1).setUV(0, 0),  // Bottom left
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
        meshBuilder.rect(
                new VertexInfo().setPos(-2, -2, -15).setNor(0, 0, 1).setUV(0, 0),  // Bottom left
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
    public Entity removeEntity(int id) {
        this.checkThread();
        ModelInstance remove = this.modelInstances.remove(id);
        if (remove == null) return null;
        RenderLayer.WORLD.destroy(remove);
        return null;
    }

    private void checkThread() {
        if (!QuantumClient.isOnRenderThread())
            throw new IllegalStateException("Should only be called on the main thread!");
    }

    /**
     * Renders the world to the screen using the provided ModelBatch and RenderLayer.
     *
     * @param batch the ModelBatch to render with
     * @param deltaTime the time between the last and current frame
     */
    @Override
    public void renderBackground(ModelBatch batch, float deltaTime) {
        updateBackground();

        batch.render(this.skybox, environment);
        batch.render(this.sun);
        batch.render(this.moon);
    }

    /**
     * Renders the world to the screen using the provided ModelBatch and RenderLayer.
     *
     * @param batch the ModelBatch to render with
     * @param renderLayer the RenderLayer to render with
     * @param deltaTime the time between the last and current frame
     */
    @Override
    public void render(ModelBatch batch, @Deprecated RenderLayer renderLayer, float deltaTime) {
        var player = this.client.player;
        if (player == null) return;
        if (this.disposed) return;

        Gdx.gl.glLineWidth(10f);

        // Update the skybox and environment.
        this.skybox.update(this.world.getDaytime(), deltaTime);
        this.environment.set(new ColorAttribute(ColorAttribute.Fog, this.skybox.bottomColor));

        // Get the loaded chunks and sort them by distance from the player.
        var chunks = WorldRenderer.chunksInViewSorted(this.world.getLoadedChunks(), player);
        this.loadedChunks = chunks.size();
        this.visibleChunks = 0;

        // Create a new ChunkRenderRef and an array of ChunkVec.
        var ref = new ChunkRenderRef();
        Array<ChunkVec> positions = new Array<>();

        // Collect the chunks to render.
        QuantumClient.PROFILER.section("chunks", () -> this.collectChunks(batch, renderLayer, chunks, positions, player, ref));

        // Render the cursor.
        BlockHit gameCursor = this.client.cursor;
        if (gameCursor != null && gameCursor.isCollide() && !this.client.hideHud && !player.isSpectator()) {
            QuantumClient.PROFILER.section("cursor", () -> {
                // Block outline.
                Vec3i pos = gameCursor.getBlockVec();
                Vec3f renderOffsetC = pos.d().sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f();
                var boundingBox = gameCursor.getBlock().getBoundingBox(0, 0, 0, gameCursor.getBlockMeta());
                renderOffsetC.add((float) boundingBox.min.x, (float) boundingBox.min.y, (float) boundingBox.min.z);

                // Render the outline.
                if (lastHitResult == null || !this.lastHitResult.equals(gameCursor)) {
                    this.lastHitResult = gameCursor;

                    if (this.cursor != null) {
                        RenderLayer.WORLD.destroy(this.cursor);
                        ModelManager.INSTANCE.unloadModel(id("generated/selection_outline"));
                    }

                    Model model = ModelManager.INSTANCE.generateModel(id("generated/selection_outline"), modelBuilder -> {
                        Material material = new Material();
                        material.id = id("generated/selection_outline_material").toString();
                        material.set(ColorAttribute.createDiffuse(0, 0, 0, 1f));
                        material.set(new BlendingAttribute(1.0f));
                        material.set(IntAttribute.createCullFace(GL_BACK));

                        var sizeX = (float) (boundingBox.max.x - boundingBox.min.x);
                        var sizeY = (float) (boundingBox.max.y - boundingBox.min.y);
                        var sizeZ = (float) (boundingBox.max.z - boundingBox.min.z);

                        WorldRenderer.buildOutlineBox(sizeX + 0.01f, sizeY + 0.01f, sizeZ + 0.01f, modelBuilder.part("outline", GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked, material));
                    });

                    this.cursor = RenderLayer.WORLD.create(model, renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);
                    this.cursor.userData = Shaders.OUTLINE.get();
                }
            });

            if (this.cursor != null) {
                batch.render(this.cursor);
            }
        }

        QuantumClient.PROFILER.section("(Local Player)", () -> {
            LocalPlayer localPlayer = this.client.player;
            if (localPlayer == null || !this.client.isInThirdPerson() && ClientConfig.hideFirstPersonPlayer) {
                if (localPlayer != null) modelInstances.remove(localPlayer.getId());
                return;
            }

            this.collectEntity(localPlayer, batch);
        });

        QuantumClient.PROFILER.section("players", () -> {
            MultiplayerData multiplayerData = this.client.getMultiplayerData();
            if (multiplayerData == null) return;
            for (var remotePlayer : multiplayerData.getRemotePlayers()) {
                QuantumClient.PROFILER.section(remotePlayer.getType().getId() + " (" + remotePlayer.getName() + ")", () -> this.collectEntity(remotePlayer, batch));
            }
        });
    }

    private void collectChunks(ModelBatch batch, RenderLayer renderLayer, List<ClientChunk> chunks, Array<ChunkVec> positions, LocalPlayer player, ChunkRenderRef ref) {
        for (var chunk : chunks) {
            if (positions.contains(chunk.getVec(), false)) {
                QuantumClient.LOGGER.warn("Duplicate chunk: {}", chunk.getVec());
                continue;
            }

            positions.add(chunk.getVec());

            if (!chunk.isReady()) continue;
            if (chunk.isDisposed()) {
                unload(chunk);
                continue;
            }

            Vec3i chunkOffset = chunk.getOffset();
            Vec3f renderOffsetC = chunkOffset.d().sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f().div(WorldRenderer.SCALE);
            chunk.renderOffset.set(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);
            if (chunk.visible && !this.client.camera.frustum.boundsInFrustum(chunk.renderOffset.cpy().add(WorldRenderer.HALF_CHUNK_DIMENSIONS), WorldRenderer.CHUNK_DIMENSIONS)) {
                chunk.visible = false;
                continue;
            } else if (!chunk.visible) {
                chunk.visible = true;
            }

            ChunkModel model = this.chunkModels.get(chunk.getVec());
            if (chunk.getWorld().isChunkInvalidated(chunk) || !chunk.initialized) {
                if (!(client.screen instanceof WorldLoadScreen || ref.chunkRendered || this.shouldIgnoreRebuild() || this.shouldIgnoreRebuild() && !chunk.immediateRebuild)) {
                    chunk.dirty = false;
                    if (model != null) {
                        if (model.rebuild()) {
                            ref.chunkRendered = true;
                            this.lastChunkBuild = System.currentTimeMillis();
                            chunk.dirty = false;
                        } else {
                            LOGGER.warn("Failed to rebuild chunk: {}", chunk.getVec());
                            continue;
                        }
                    } else {
                        CommonConstants.LOGGER.warn("Tried to rebuild a chunk that didn't exist: " + chunk.getVec());
                        model = new ChunkModel(chunk.getVec(), chunk, this);
                        if (model.build()) {
                            ref.chunkRendered = true;
                            this.lastChunkBuild = System.currentTimeMillis();
                            chunk.dirty = false;
                            this.chunkModels.put(chunk.getVec(), model);
                        } else {
                            LOGGER.warn("Failed to build chunk: {}", chunk.getVec());
                            continue;
                        }
                    }

                    chunk.onUpdated();
                    chunk.initialized = true;
                }
            } else if (model == null) {
                if (ref.chunkRendered || this.shouldIgnoreRebuild()) continue;
                chunk.dirty = false;
                model = new ChunkModel(chunk.getVec(), chunk, this);
                if (model.build()) {
                    ref.chunkRendered = true;
                    this.lastChunkBuild = System.currentTimeMillis();
                    chunk.dirty = false;
                    chunk.initialized = true;
                    this.chunkModels.put(chunk.getVec(), model);
                } else {
                    LOGGER.warn("Failed to build chunk: {}", chunk.getVec());
                    continue;
                }
            } else if (model.needsRebuild(world) && !(ref.chunkRendered || this.shouldIgnoreRebuild())) {
                if (model.rebuild()) {
                    ref.chunkRendered = true;
                    this.lastChunkBuild = System.currentTimeMillis();
                    chunk.dirty = false;
                    chunk.onUpdated();
                    chunk.initialized = true;
                } else {
                    LOGGER.warn("Failed to rebuild chunk: {}", chunk.getVec());
                }
                continue;
            }

            this.renderBlockBreaking(batch, chunk);
            this.renderBlockModels(batch, chunk);

            if (model != null) {
                model.getModelInstance().transform.setTranslation(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);
                batch.render(model, this.environment);
            }

            chunk.renderModels(renderLayer);

            this.visibleChunks++;
        }
    }

    void unload(ClientChunk chunk) {
        if (!QuantumClient.isOnRenderThread()) {
            QuantumClient.invoke(() -> this.unload(chunk));
            return;
        }

        if (client.screen instanceof WorldLoadScreen) {
            QuantumClient.LOGGER.warn("Chunk unloaded when loading game: ", new Throwable());
        }

        ChunkModel chunkModel = chunkModels.remove(chunk.getVec());
        if (chunkModel == null) {
            LOGGER.warn("Tried to unload a chunk that didn't exist: " + chunk.getVec());
            return;
        }
        if (RenderLayer.WORLD.destroy(chunkModel.getModelInstance()))
            throw new DeprecationCheckException("World render layer shouldn't have a chunk model instance: " + chunk.getVec());
        if (chunkModel.getChunk() != chunk)
            throw new DeprecationCheckException("Model's chunk and chunk mismatch: " + chunk.getVec());


        chunkModel.dispose();

        NamespaceID id = createId(chunk.getVec());
        if (!ModelManager.INSTANCE.unloadModel(id)) {
            QuantumClient.LOGGER.warn("Didn't find chunk model {} to dispose, possibly it didn't exist, or got moved out.", id);
        }

        Map<BlockVec, BlockState> customRendered = chunk.getCustomRendered();
        for (var entry : blockInstances.entrySet()) {
            if (customRendered.containsKey(entry.getKey())) {
                ModelInstance value = entry.getValue();
                RenderLayer.WORLD.destroy(value);
                blockInstances.remove(entry.getKey());
            }
        }

        ValueTracker.setChunkMeshFrees(ValueTracker.getChunkMeshFrees() + 1);
    }

    private static @NotNull NamespaceID createId(ChunkVec pos) {
        return id(("generated/chunk/" + pos.getIntX() + "." + pos.getIntZ()).replace('-', '_'));
    }

    private void renderBlockModels(ModelBatch batch, ClientChunk chunk) {
        for (var entry : chunk.getCustomRendered().entrySet()) {
            BlockVec localVec = entry.getKey();
            Vector3 translation = this.tmp.set(chunk.renderOffset).add(localVec.getIntX(), localVec.getIntY(), localVec.getIntZ());

            BlockState blockState = entry.getValue();
            BlockModel blockModel = BlockModelRegistry.get().get(blockState);
            BlockVec globalVec = chunk.getVec().blockInWorldSpace(localVec);
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

            batch.render(modelInstance, this.environment);
        }
    }

    private void renderBlockBreaking(ModelBatch batch, ClientChunk chunk) {
        for (var entry : this.breakingInstances.entrySet()) {
            BlockVec pos = entry.getKey();

            Model breakingMesh = this.breakingModels.get(Math.round(Mth.clamp(1.0f * 5, 0, 5)));
            ModelInstance modelInstance = new ModelInstance(breakingMesh, this.tmp.x, this.tmp.y, this.tmp.z);
            Vector3 translation = this.tmp.set(chunk.renderOffset).add(pos.getIntX() + 1f, pos.getIntY(), pos.getIntZ());
            modelInstance.transform.setToTranslationAndScaling(translation, this.tmp2.set(1.1f, 1.1f, 1.1f));
            modelInstance.userData = Shaders.MODEL_VIEW.get();

            batch.render(modelInstance, environment);
        }
    }

    private boolean shouldIgnoreRebuild() {
        return this.lastChunkBuild >= System.currentTimeMillis() - 375L;
    }

    @Override
    public void collectEntity(Entity entity, ModelBatch batch) {
        try {
            @Nullable QVModel model = this.qvModels.get(entity.getId());
            LocalPlayer player = QuantumClient.get().player;
            if (player == null
                || player.getPosition(client.partialTick).dst(entity.getPosition()) > 64
                || entity instanceof Player && ((Player) entity).isSpectator()) {
                if (model != null)
                    return;
                return;
            }

            //noinspection unchecked
            var renderer = (EntityRenderer<@NotNull Entity>) this.client.entityRendererManager.get(entity.getType());
            if (model == null) {
                if (renderer == null) {
                    QuantumClient.LOGGER.warn("Failed to render entity " + entity.getId() + " because it's renderer is null");
                    return;
                }
                model = renderer.createModel(entity);
                if (model == null) {
                    QuantumClient.LOGGER.warn("Failed to render entity {} because it's model instance is still null", entity.getId());
                    return;
                }
                this.modelInstances.put(entity.getId(), model.getInstance());
                this.qvModels.put(entity.getId(), model);
            }

            EntityModelInstance<@NotNull Entity> instance = new EntityModelInstance<>(model, entity);
            WorldRenderContextImpl<Entity> context = new WorldRenderContextImpl<>(batch, entity, entity.getWorld(), WorldRenderer.SCALE, player.getPosition(client.partialTick));
            renderer.animate(instance, context);
            renderer.render(instance, context);
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

    public static void buildLine(float thickness, float x1, float y1, float z1, float x2, float y2, float z2, MeshPartBuilder meshBuilder) {
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(x1 - thickness, y1 - thickness, z1 - thickness), new Vector3(x2 + thickness, y2 + thickness, z2 + thickness)));
    }

    @NotNull
    private static List<ClientChunk> chunksInViewSorted(Collection<ClientChunk> chunks, Player player) {
        List<ClientChunk> list = new ArrayList<>(chunks);
        list = list.stream().sorted((o1, o2) -> {
            Vec3d mid1 = WorldRenderer.TMP_3D_A.set(o1.getOffset().x + (float) CHUNK_SIZE, o1.getOffset().y + (float) CHUNK_HEIGHT, o1.getOffset().z + (float) CHUNK_SIZE);
            Vec3d mid2 = WorldRenderer.TMp_3D_B.set(o2.getOffset().x + (float) CHUNK_SIZE, o2.getOffset().y + (float) CHUNK_HEIGHT, o2.getOffset().z + (float) CHUNK_SIZE);
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
        for (var entry : List.copyOf(chunkModels.entrySet())) {
            unload(entry.getValue().getChunk());
        }
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

    @Override
    public void dispose() {
        this.disposed = true;

        Model skybox = this.skybox.model;
        this.skybox.model = null;
        this.skybox.modelInstance = null;
        skybox.dispose();

        ModelManager.INSTANCE.unloadModel(id("generated/skybox"));

        for (var entry : chunkModels.entrySet()) {
            ClientChunk first = entry.getValue().getChunk();
            unload(first);
        }

        RenderLayer.WORLD.clear();
        RenderLayer.BACKGROUND.clear();

        this.modelInstances.clear();
        this.blockInstances.clear();
        this.breakingInstances.clear();

        this.disposables.forEach(Disposable::dispose);
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

    public Texture getBreakingTex() {
        return this.breakingTex;
    }

    public Material getMaterial() {
        return this.material;
    }

    public Material getTransparentMaterial() {
        return this.transparentMaterial;
    }

    @Override
    public <T extends Disposable> T deferDispose(T disposable) {
        Preconditions.checkNotNull(disposable, "Disposable cannot be null");

        if (this.disposables.contains(disposable)) return disposable;
        if (this.disposed) {
            QuantumClient.LOGGER.warn("World renderer already disposed, immediately disposing {}", disposable.getClass().getName());
            disposable.dispose();
            return disposable;
        }
        this.disposables.add(disposable);
        return disposable;
    }

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

            RenderLayer.BACKGROUND.destroy(moon);
            RenderLayer.BACKGROUND.destroy(sun);
            RenderLayer.BACKGROUND.clear();

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

    private void unload(ChunkVec chunkVec) {
        ClientChunk clientChunk = this.world.getChunk(chunkVec);
        if (clientChunk != null) this.unload(clientChunk);
    }

    @Override
    public Skybox getSkybox() {
        return skybox;
    }

    @Override
    public void updateBackground() {
        if (ClientConfig.showSunAndMoon) {
            if (!wasSunMoonShown) {
                RenderLayer.BACKGROUND.activate(this.sun);
                RenderLayer.BACKGROUND.activate(this.moon);
            }

            wasSunMoonShown = true;

            material.set(new DepthTestAttribute(GL_LEQUAL, true));
            var world = this.world;
            if (world == null) return;

            int daytime = world.getDaytime();

            // Sun angle
            float sunAngle = (float) ((daytime % 24000) / 24000.0 * Math.PI * 2);

            // Moon on the opposite side
            float moonAngle = (float) ((daytime % 24000) / 24000.0 * Math.PI * 2);

            this.sun.transform.setToRotation(Vector3.Z, ClientWorld.SKYBOX_ROTATION.getDegrees()).rotate(Vector3.Y, sunAngle * MathUtils.radDeg - 180);
            this.moon.transform.setToRotation(Vector3.Z, ClientWorld.SKYBOX_ROTATION.getDegrees()).rotate(Vector3.Y, moonAngle * MathUtils.radDeg - 180);

            this.sunDirection.setZero().setLength(1).rotate(Vector3.Z, ClientWorld.SKYBOX_ROTATION.getDegrees()).rotate(Vector3.Y, sunAngle * MathUtils.radDeg - 180);
        } else if (wasSunMoonShown) {
            RenderLayer.BACKGROUND.deactivate(this.sun);
            RenderLayer.BACKGROUND.deactivate(this.moon);

            wasSunMoonShown = false;
        }
    }

    @Override
    public void remove(ClientChunkAccess clientChunk) {
        this.unload(clientChunk.getVec());
    }

    @Override
    public void addParticles(ParticleEffect obtained, Vec3d position, Vec3d motion, int count) {
        LocalPlayer player = client.player;
        if (player == null) return;
        Vec3f div = position.sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f().div(WorldRenderer.SCALE);

        Vector3 vector3 = new Vector3(div.x, div.y, div.z);
        obtained.translate(vector3);
        particleSystem.add(obtained);
    }

    @Override
    public void unload(ClientChunkAccess clientChunk) {
        ChunkModel remove = this.chunkModels.remove(clientChunk.getVec());

        if (remove != null) {
            remove.dispose();
        }
    }

    @Override
    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    private static class ChunkRenderRef {
        boolean chunkRendered = false;
    }
}
