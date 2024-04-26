package com.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.base.Preconditions;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.client.DisposableContainer;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.config.Config;
import com.ultreon.quantum.client.gui.screens.WorldLoadScreen;
import com.ultreon.quantum.client.imgui.ImGuiOverlay;
import com.ultreon.quantum.client.render.shader.Shaders;
import com.ultreon.quantum.client.model.EntityModelInstance;
import com.ultreon.quantum.client.model.QVModel;
import com.ultreon.quantum.client.model.WorldRenderContextImpl;
import com.ultreon.quantum.client.model.block.BakedCubeModel;
import com.ultreon.quantum.client.model.block.BlockModel;
import com.ultreon.quantum.client.model.block.BlockModelRegistry;
import com.ultreon.quantum.client.model.entity.renderer.EntityRenderer;
import com.ultreon.quantum.client.multiplayer.MultiplayerData;
import com.ultreon.quantum.client.player.LocalPlayer;
import com.ultreon.quantum.client.management.MaterialManager;
import com.ultreon.quantum.client.render.Models3D;
import com.ultreon.quantum.client.render.Scene3D;
import com.ultreon.quantum.resources.ReloadContext;
import com.ultreon.quantum.crash.CrashCategory;
import com.ultreon.quantum.crash.CrashLog;
import com.ultreon.quantum.debug.ValueTracker;
import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.util.BlockHitResult;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.ChunkPos;
import com.ultreon.quantum.world.World;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.ultreon.quantum.client.QuantumClient.crash;
import static com.ultreon.quantum.client.QuantumClient.id;
import static com.ultreon.quantum.world.World.*;

public final class WorldRenderer implements DisposableContainer {
    public static final float SCALE = 1;
    private static final Vec3d TMP_3D_A = new Vec3d();
    private static final Vec3d TMp_3D_B = new Vec3d();
    public static final String OUTLINE_CURSOR_ID = CommonConstants.strId("outline_cursor");
    public static final int QV_CHUNK_ATTRS = VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.Normal;
    public ParticleSystem particleSystem = new ParticleSystem();
    public ParticleController particleController = new ParticleController("world", new RegularEmitter(), new BillboardRenderer());
    private Material material;
    private Material transparentMaterial;
    private final Texture breakingTex;
    private final Environment environment;
    private int visibleChunks;
    private int loadedChunks;
    private static final Vector3 CHUNK_DIMENSIONS = new Vector3(CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE);
    private static final Vector3 HALF_CHUNK_DIMENSIONS = WorldRenderer.CHUNK_DIMENSIONS.cpy().scl(0.5f);

    private final ClientWorld world;
    private final QuantumClient client = QuantumClient.get();

    private ModelInstance skyboxInstance = null;
    private ModelInstance cursor = null;
    private ModelInstance sun = null;
    private ModelInstance moon = null;
    private boolean disposed = false;
    private final Vector3 tmp = new Vector3();
    private Material breakingMaterial;
    private final Array<Model> breakingModels = new Array<>();
    private final Int2ObjectMap<ModelInstance> modelInstances = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<QVModel> qvModels = new Int2ObjectOpenHashMap<>();
    private final List<Disposable> disposables = new ArrayList<>();
    private long lastChunkBuild;
    private final Skybox skybox = new Skybox();
    private BlockHitResult lastHitResult;
    private final Map<BlockPos, ModelInstance> breakingInstances = new HashMap<>();
    private final Map<BlockPos, ModelInstance> blockInstances = new ConcurrentHashMap<>();
    private final Array<ClientChunk> removedChunks = new Array<>();
    private final Map<ChunkPos, Pair<ClientChunk, ModelInstance>> chunkModels = new ConcurrentHashMap<>();
    private final boolean wasSunMoonShown = true;

    public WorldRenderer(ClientWorld world) {
        this.world = world;

        Texture blockTex = this.client.blocksTextureAtlas.getTexture();
        Texture emissiveBlockTex = this.client.blocksTextureAtlas.getEmissiveTexture();

        this.setupMaterials(blockTex, emissiveBlockTex);

        // Dynamic Skybox
        this.skyboxInstance = this.setupDynamicSkybox();

        // Sun and moon
        this.setupSunAndMoon();

        // Breaking animation meshes.
        this.breakingTex = this.client.getTextureManager().getTexture(id("textures/break_stages.png"));
        this.breakingMaterial = this.client.getMaterialManager().get(id("block/breaking"));

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
            BakedCubeModel bakedCubeModel = this.deferDispose(BakedCubeModel.of(new Identifier("break_stage/stub_" + i), breakingTexRegions.get(i)));
            Mesh mesh = bakedCubeModel.getMesh();
            Model model = Models3D.INSTANCE.generateModel(id("generated/breaking/stage_" + i), modelBuilder -> {
                modelBuilder.part("breaking", mesh, GL_TRIANGLES, this.breakingMaterial);
            });

            this.breakingModels.add(model);
        }

        QuantumClient.LOGGER.info("Setting up world environment");

        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
        this.environment.set(new ColorAttribute(ColorAttribute.Fog, 0.6F, 0.7F, 1.0F, 1.0F));
        this.environment.set(new ColorAttribute(ColorAttribute.Specular, 1, 1, 1, 1f));
    }

    private ModelInstance setupDynamicSkybox() {
        Models3D models3D = Models3D.INSTANCE;
        Model model = models3D.generateModel(id("generated/skybox"), modelBuilder -> {
            Material material = new Material();
            material.id = id("generated/skybox_material").toString();
            material.set(ColorAttribute.createDiffuse(0, 1, 0, 1));
            material.set(new BlendingAttribute());
            material.set(new DepthTestAttribute(GL_LEQUAL, true));
            material.set(IntAttribute.createCullFace(0));

            return modelBuilder.createBox(60, 60, 60, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked);
        });

        Scene3D background = Scene3D.BACKGROUND;
        ModelInstance modelInstance = background.create(model, 0, 0, 0);
        modelInstance.userData = Shaders.SKYBOX.get();
        return modelInstance;
    }

    private void setupMaterials(Texture blockTex, Texture emissiveBlockTex) {
        this.material = new Material();
        this.material.set(TextureAttribute.createDiffuse(blockTex));
        this.material.set(TextureAttribute.createEmissive(emissiveBlockTex));
        this.material.set(new DepthTestAttribute(GL_DEPTH_FUNC));
        this.transparentMaterial = new Material();
        this.transparentMaterial.set(TextureAttribute.createDiffuse(blockTex));
        this.transparentMaterial.set(TextureAttribute.createEmissive(emissiveBlockTex));
        this.transparentMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.transparentMaterial.set(new DepthTestAttribute(GL_DEPTH_FUNC));
//        this.transparentMaterial.set(FloatAttribute.createAlphaTest(0.01f));
    }

    private void setupSunAndMoon() {
        Model sunModel = Models3D.INSTANCE.generateModel(id("generated/sun"), modelBuilder -> {
            Material sunMat = new Material();
            sunMat.id = id("generated/sun_material").toString();
            sunMat.set(TextureAttribute.createDiffuse(this.client.getTextureManager().getTexture(id("textures/environment/sun.png"))));
            sunMat.set(TextureAttribute.createEmissive(this.client.getTextureManager().getTexture(id("textures/environment/sun.png"))));
            sunMat.set(new DepthTestAttribute(GL_LEQUAL, true));
            sunMat.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
            sunMat.set(IntAttribute.createCullFace(0));
            sunMat.set(FogAttribute.createFog(1, 1, 1));

            modelBuilder.part(id("generated/sun_part").toString(), createSun(), GL_TRIANGLES, sunMat);
        });

        Model moonModel = Models3D.INSTANCE.generateModel(id("generated/moon"), modelBuilder -> {
            Material moonMat = new Material();
            moonMat.id = id("generated/moon_material").toString();
            moonMat.set(TextureAttribute.createDiffuse(this.client.getTextureManager().getTexture(id("textures/environment/moon.png"))));
            moonMat.set(TextureAttribute.createEmissive(this.client.getTextureManager().getTexture(id("textures/environment/moon.png"))));
            moonMat.set(new DepthTestAttribute(GL_LEQUAL, true));
            moonMat.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
            moonMat.set(IntAttribute.createCullFace(0));
            moonMat.set(FogAttribute.createFog(1, 1, 1));

            modelBuilder.part(id("generated/moon_part").toString(), createMoon(), GL_TRIANGLES, moonMat);
        });

        this.sun = Scene3D.BACKGROUND.create(sunModel, 0, 0, 0);
        this.moon = Scene3D.BACKGROUND.create(moonModel, 0, 0, 0);
    }

    private Mesh createSun() {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);

        meshBuilder.rect(
                new VertexInfo().setPos(-2, -2, 15).setNor(0, 0, -1).setUV(0, 0),
                new VertexInfo().setPos(-2, 2, 15).setNor(0, 0, -1).setUV(0, 1),
                new VertexInfo().setPos(2, 2, 15).setNor(0, 0, -1).setUV(1, 1),
                new VertexInfo().setPos(2, -2, 15).setNor(0, 0, -1).setUV(1, 0)
        );

        return meshBuilder.end();
    }

    private Mesh createMoon() {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);

        meshBuilder.rect(
                new VertexInfo().setPos(-2, -2, -15).setNor(0, 0, 1).setUV(0, 0),
                new VertexInfo().setPos(-2, 2, -15).setNor(0, 0, 1).setUV(0, 1),
                new VertexInfo().setPos(2, 2, -15).setNor(0, 0, 1).setUV(1, 1),
                new VertexInfo().setPos(2, -2, -15).setNor(0, 0, 1).setUV(1, 0)
        );

        return meshBuilder.end();
    }

    @NotNull
    private MeshMaterial createChunkOutline() {
        Mesh mesh = this.deferDispose(WorldRenderer.buildOutlineBox(2 / 16f, CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE));

        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(0, 0f, 0f, 0.25f));
        material.set(new BlendingAttribute());
        material.set(new DepthTestAttribute(false));
        return new MeshMaterial(mesh, material);
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public static long getChunkMeshFrees() {
        return ValueTracker.getChunkMeshFrees();
    }

    public static long getVertexCount() {
        return ValueTracker.getVertexCount();
    }

    public void free(ClientChunk chunk) {
        if (!QuantumClient.isOnMainThread()) {
            QuantumClient.invoke(() -> this.free(chunk));
            return;
        }

        if (!chunk.initialized) return;

//        ModelInstance modelInstance = chunk.modelInstance;
//        if (modelInstance == null)
//            QuantumClient.LOGGER.warn("Model instance is null for chunk {}, not disposing it.", chunk.getPos());
//        else Scene3D.WORLD.destroy(modelInstance);
//
//        chunk.modelInstance = null;
//
//        Identifier id = createId(chunk.getPos());
//        if (!Models3D.INSTANCE.unloadModel(id)) {
//            QuantumClient.LOGGER.warn("Didn't find chunk model {} to dispose, possibly it didn't exist, or got moved out.", id);
//        }
//
//        chunk.model = null;

        chunk.initialized = false;
        ValueTracker.setChunkMeshFrees(ValueTracker.getChunkMeshFrees() + 1);
    }

    public void removeEntity(int id) {
        this.checkThread();
        ModelInstance remove = this.modelInstances.remove(id);
        if (remove == null) return;
        Scene3D.WORLD.destroy(remove);
    }

    private void checkThread() {
        if (!QuantumClient.isOnMainThread())
            throw new IllegalStateException("Should only be called on the main thread!");
    }

    public void render(Scene3D scene3D) {
        var player = this.client.player;
        if (player == null) return;
        if (this.disposed) return;

        this.skybox.update(this.world.getDaytime());
        this.environment.set(new ColorAttribute(ColorAttribute.Fog, this.skybox.bottomColor));

        var chunks = WorldRenderer.chunksInViewSorted(this.world.getLoadedChunks(), player);
        this.loadedChunks = chunks.size();
        this.visibleChunks = 0;

        var ref = new ChunkRenderRef();

        Array<ChunkPos> positions = new Array<>();
        QuantumClient.PROFILER.section("chunks", () -> this.collectChunks(scene3D, chunks, positions, player, ref));

        BlockHitResult gameCursor = this.client.cursor;
        if (gameCursor != null && gameCursor.isCollide() && !this.client.hideHud && !player.isSpectator()) {
            QuantumClient.PROFILER.section("cursor", () -> {
                // Block outline.
                Vec3i pos = gameCursor.getPos();
                Vec3f renderOffsetC = pos.d().sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f();
                var boundingBox = gameCursor.getBlock().getBoundingBox(0, 0, 0, gameCursor.getBlockMeta());
                renderOffsetC.add((float) boundingBox.min.x, (float) boundingBox.min.y, (float) boundingBox.min.z);

                if (lastHitResult == null || !this.lastHitResult.equals(gameCursor)) {
                    this.lastHitResult = gameCursor;

                    if (this.cursor != null) {
                        Scene3D.WORLD.destroy(this.cursor);
                        Models3D.INSTANCE.unloadModel(id("generated/selection_outline"));
                    }

                    Model model = Models3D.INSTANCE.generateModel(id("generated/selection_outline"), modelBuilder -> {
                        Material material = new Material();
                        material.id = id("generated/selection_outline_material").toString();
                        material.set(ColorAttribute.createDiffuse(0, 0, 0, 1f));
                        material.set(new BlendingAttribute(1.0f));
                        material.set(IntAttribute.createCullFace(GL_BACK));

                        var sizeX = (float) (boundingBox.max.x - boundingBox.min.x);
                        var sizeY = (float) (boundingBox.max.y - boundingBox.min.y);
                        var sizeZ = (float) (boundingBox.max.z - boundingBox.min.z);


                        WorldRenderer.buildOutlineBox(0.02f, sizeX, sizeY, sizeZ, modelBuilder.part("outline", GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked, material));
                    });

                    this.cursor = Scene3D.WORLD.create(model, renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);
                }
            });
        } else if (this.cursor != null) {
            Scene3D.WORLD.deactivate(this.cursor);
        }

        QuantumClient.PROFILER.section("(Local Player)", () -> {
            LocalPlayer localPlayer = this.client.player;
            if (localPlayer == null || !this.client.isInThirdPerson() && Config.hideFirstPersonPlayer) {
                if (localPlayer != null) {
                    scene3D.deactivate(modelInstances.get(localPlayer.getId()));
                }
                return;
            }

            this.collectEntity(localPlayer, scene3D);
        });

        QuantumClient.PROFILER.section("players", () -> {
            MultiplayerData multiplayerData = this.client.getMultiplayerData();
            if (multiplayerData == null) return;
            for (var remotePlayer : multiplayerData.getRemotePlayers()) {
                QuantumClient.PROFILER.section(remotePlayer.getType().getId() + " (" + remotePlayer.getName() + ")", () -> {
                    // TODO: Implement if needed
                });
            }
        });
    }

    private void collectChunks(Scene3D scene3D, List<ClientChunk> chunks, Array<ChunkPos> positions, LocalPlayer player, ChunkRenderRef ref) {
        for (var chunk : this.removedChunks) {
            if (chunk.modelInstance != null) {
                scene3D.destroy(chunk.modelInstance);
            }
        }

        for (var chunk : chunks) {
            if (positions.contains(chunk.getPos(), false)) {
                QuantumClient.LOGGER.warn("Duplicate chunk: {}", chunk.getPos());
                continue;
            }

            positions.add(chunk.getPos());

            if (!chunk.isReady()) continue;
            if (chunk.isDisposed()) {
                if (chunk.modelInstance != null) {
                    unload(chunk);
                }
                continue;
            }

            Vec3i chunkOffset = chunk.getOffset();
            Vec3f renderOffsetC = chunkOffset.d().sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f().div(WorldRenderer.SCALE);
            chunk.renderOffset.set(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);
            if (chunk.visible && !this.client.camera.frustum.boundsInFrustum(chunk.renderOffset.cpy().add(WorldRenderer.HALF_CHUNK_DIMENSIONS), WorldRenderer.CHUNK_DIMENSIONS)) {
                Scene3D.WORLD.deactivate(chunk.modelInstance);
                chunk.visible = false;
                continue;
            } else if (!chunk.visible) {
                Scene3D.WORLD.activate(chunk.modelInstance);
                chunk.visible = true;
            }

            if ((chunk.dirty && !ref.chunkRendered && chunk.modelInstance != null) || (chunk.modelInstance != null && chunk.getWorld().isChunkInvalidated(chunk))) {
                if (client.screen instanceof WorldLoadScreen) continue;
                this.unload(chunk);
                chunk.immediateRebuild = true;
                chunk.getWorld().onChunkUpdated(chunk);
                chunk.dirty = false;
                ref.chunkRendered = true;
            }

            chunk.dirty = false;

            if (chunk.modelInstance == null) {
                if (!this.shouldBuildChunks() && !chunk.immediateRebuild) continue;
                chunk.immediateRebuild = false;
                chunk.whileLocked(() -> {
                    if (chunk.modelInstance == null) {
                        Models3D models3D = Models3D.INSTANCE;
                        ChunkPos pos = chunk.getPos();
                        Model model = models3D.generateModel(createId(pos), modelBuilder -> {
                            chunk.mesher.meshVoxels(modelBuilder,
                                    modelBuilder.part("solid:" + createId(pos), GL_TRIANGLES, QV_CHUNK_ATTRS, this.material),
                                    block -> block.doesRender() && !block.isTransparent()
                            );

                            chunk.mesher.meshVoxels(modelBuilder,
                                    modelBuilder.part("transparent:" + createId(pos), GL_TRIANGLES, QV_CHUNK_ATTRS, this.transparentMaterial),
                                    block -> block.doesRender() && block.isTransparent()
                            );
                        });

                        if (model == null) {
                            throw new IllegalStateException("Failed to generate chunk model: " + pos);
                        }

                        chunk.model = model;
                        chunk.modelInstance = Scene3D.WORLD.create(model);
                        chunk.modelInstance.transform.setTranslation(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);

                        chunk.modelInstance.userData = chunk;

                        this.chunkModels.put(pos, new Pair<>(chunk, chunk.modelInstance));
                    }
                    chunk.loadCustomRendered();

                    chunk.dirty = false;
                    chunk.onUpdated();
                    chunk.initialized = true;
                    this.lastChunkBuild = System.currentTimeMillis();
                });
            }

            this.renderBlockBreaking(scene3D, chunk);
            this.renderBlockModels(scene3D, chunk);

            chunk.modelInstance.transform.setTranslation(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);

            chunk.renderModels(scene3D);

            if (ImGuiOverlay.isChunkSectionBordersShown()) {
//                this.tmp.set(chunk.renderOffset);
//                Mesh mesh = this.sectionBorder;
//
//                int numIndices = mesh.getNumIndices();
//                int numVertices = mesh.getNumVertices();
//                Renderable renderable = renderablePool.obtain();
//                renderable.meshPart.mesh = mesh;
//                renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
//                renderable.meshPart.offset = 0;
//                renderable.meshPart.primitiveType = GL_TRIANGLES;
//                renderable.material = this.sectionBorderMaterial;
//                Vector3 add = this.tmp.add(0, -WORLD_DEPTH, 0);
//                renderable.worldTransform.setToTranslationAndScaling(add, this.tmp1.set(1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE));
//
//                output.add(verifyOutput(renderable));
            }

            this.visibleChunks++;
        }
    }

    void unload(ClientChunk chunk) {
        if (!QuantumClient.isOnMainThread()) {
            QuantumClient.invoke(() -> this.unload(chunk));
            return;
        }

        if (!chunk.initialized) return;

        if (client.screen instanceof WorldLoadScreen) {
            QuantumClient.LOGGER.warn("Chunk unloaded when loading game: ", new Throwable());
        }

        chunk.initialized = false;

        ModelInstance modelInstance = chunk.modelInstance;
        if (modelInstance == null)
            QuantumClient.LOGGER.warn("Model instance is null for chunk {}, not disposing it.", chunk.getPos());
        else if (!Scene3D.WORLD.destroy(modelInstance))
            QuantumClient.LOGGER.warn("Model instance didn't exist in scene! Chunk at {}", chunk.getPos());

        chunk.modelInstance = null;
        chunk.model = null;

        chunk.destroyModels();

        Identifier id = createId(chunk.getPos());
        if (!Models3D.INSTANCE.unloadModel(id)) {
            QuantumClient.LOGGER.warn("Didn't find chunk model {} to dispose, possibly it didn't exist, or got moved out.", id);
        }


        Map<BlockPos, BlockProperties> customRendered = chunk.getCustomRendered();
        for (var entry : blockInstances.entrySet()) {
            if (customRendered.containsKey(entry.getKey())) {
                ModelInstance value = entry.getValue();
                Scene3D.WORLD.destroy(value);
                blockInstances.remove(entry.getKey());
            }
        }

        ValueTracker.setChunkMeshFrees(ValueTracker.getChunkMeshFrees() + 1);
    }

    private static @NotNull Identifier createId(ChunkPos pos) {
        return id(("generated/chunk/" + pos.x() + "." + pos.z()).replace('-', '_'));
    }

    private void renderBlockModels(Scene3D scene3D, ClientChunk chunk) {
        for (var entry : chunk.getCustomRendered().entrySet()) {
            BlockPos key = entry.getKey();
            this.tmp.set(chunk.renderOffset);
            this.tmp.add(key.x(), key.y(), key.z());

            BlockProperties value = entry.getValue();
            BlockModel blockModel = BlockModelRegistry.get(value);
            if (!blockInstances.containsKey(key) && blockModel != null) {
                Model model = blockModel.getModel();
                if (model != null) {
                    ModelInstance modelInstance = scene3D.create(model, this.tmp);
                    this.blockInstances.put(key, modelInstance);
                }
            }

            ModelInstance modelInstance = blockInstances.get(key);
            modelInstance.userData = Shaders.MODEL_VIEW.get();
            modelInstance.transform.setTranslation(this.tmp.set(chunk.renderOffset).add(key.x(), key.y(), key.z()));
        }

        for (var entry : this.blockInstances.entrySet()) {
            if (!chunk.getCustomRendered().containsKey(entry.getKey())) {
                scene3D.destroy(entry.getValue());
            }
        }
    }

    private void renderBlockBreaking(Scene3D scene3D, ClientChunk chunk) {
        Map<BlockPos, Float> breaking = chunk.getBreaking();
        for (var entry : breaking.entrySet()) {
            BlockPos key = entry.getKey();
            this.tmp.set(chunk.renderOffset);
            this.tmp.add(key.x() + 1f, key.y(), key.z());

            Model breakingMesh = this.breakingModels.get(Math.round(Mth.clamp(entry.getValue() * 5, 0, 5)));
            if (!breakingInstances.containsKey(key)) {
                ModelInstance modelInstance = scene3D.create(breakingMesh, this.tmp.x, this.tmp.y, this.tmp.z);
                this.breakingInstances.put(key, modelInstance);
            }
        }

        for (var entry : this.breakingInstances.entrySet()) {
            if (!breaking.containsKey(entry.getKey())) {
                scene3D.destroy(entry.getValue());
            }
        }
    }

    private boolean shouldBuildChunks() {
        return this.lastChunkBuild < System.currentTimeMillis() - 100L;
    }

    public void collectEntity(Entity entity, Scene3D scene3D) {
        try {
            @Nullable QVModel model = this.qvModels.get(entity.getId());
            LocalPlayer player = QuantumClient.get().player;
            if (player == null
                    || player.getPosition(client.partialTick).dst(entity.getPosition()) > 64
                    || entity instanceof Player playerEntity && playerEntity.isSpectator()) {
                if (model != null)
                    scene3D.deactivate(model.getInstance());
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
                scene3D.add(model);
            }

            scene3D.activate(model.getInstance());

            EntityModelInstance<@NotNull Entity> instance = new EntityModelInstance<>(model, entity);
            WorldRenderContextImpl<Entity> context = new WorldRenderContextImpl<>(scene3D, entity, entity.getWorld(), WorldRenderer.SCALE, player.getPosition(client.partialTick));
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

    public static Renderable verifyOutput(Renderable renderable) {
        Preconditions.checkNotNull(renderable.meshPart.mesh, "Mesh of renderable is null");
        Preconditions.checkNotNull(renderable.material, "Material of renderable is null");
        return renderable;
    }

    public static Mesh buildOutlineBox(float thickness) {
        return WorldRenderer.buildOutlineBox(thickness, 1, 1, 1);
    }

    public static Mesh buildOutlineBox(float thickness, float width, float height, float depth) {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(new VertexAttributes(VertexAttribute.Position()), GL_TRIANGLES);

        WorldRenderer.buildOutlineBox(thickness, width, height, depth, meshBuilder);

        // Create the mesh from the mesh builder
        return meshBuilder.end();
    }

    public static void buildOutlineBox(float thickness, float width, float height, float depth, MeshPartBuilder meshBuilder) {
        // Top face
        buildLine(thickness, 0, height, 0, width, height, 0, meshBuilder);
        buildLine(thickness, 0, height, depth, width, height, depth, meshBuilder);
        buildLine(thickness, width, height, 0, width, height, depth, meshBuilder);
        buildLine(thickness, 0, height, 0, 0, height, depth, meshBuilder);

        // Bottom face
        buildLine(thickness, 0, 0, 0, width, 0, 0, meshBuilder);
        buildLine(thickness, 0, 0, depth, width, 0, depth, meshBuilder);
        buildLine(thickness, width, 0, 0, width, 0, depth, meshBuilder);
        buildLine(thickness, 0, 0, 0, 0, 0, depth, meshBuilder);

        // Sides
        buildLine(thickness, 0, 0, 0, 0, height, 0, meshBuilder);
        buildLine(thickness, width, 0, 0, width, height, 0, meshBuilder);
        buildLine(thickness, 0, 0, depth, 0, height, depth, meshBuilder);
        buildLine(thickness, width, 0, depth, width, height, depth, meshBuilder);
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
        }).toList();
        return list;
    }

    public int getVisibleChunks() {
        return this.visibleChunks;
    }

    public int getLoadedChunks() {
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

    public World getWorld() {
        return this.world;
    }

    @Override
    public void dispose() {
        this.disposed = true;

        Scene3D.BACKGROUND.destroy(this.skyboxInstance);
        Models3D.INSTANCE.unloadModel(id("generated/skybox"));

        for (var entry : chunkModels.entrySet()) {
            ClientChunk first = entry.getValue().getFirst();
            unload(first);
        }

        Scene3D.WORLD.clear();
        Scene3D.BACKGROUND.clear();

        this.disposables.forEach(Disposable::dispose);
    }

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

    public void reload(ReloadContext context, MaterialManager materialManager) {
        context.submit(() -> {
            this.breakingMaterial = materialManager.get(id("block/breaking"));

            Texture blockTex = this.client.blocksTextureAtlas.getTexture();
            Texture emissiveBlockTex = this.client.blocksTextureAtlas.getEmissiveTexture();

            setupMaterials(blockTex, emissiveBlockTex);

            // TODO Implement reloading for chunks

            this.modelInstances.clear();
        });
    }

    public Skybox getSkybox() {
        return skybox;
    }

    public void updateBackground() {
        if (Config.showSunAndMoon) {
            if (!wasSunMoonShown) {
                Scene3D.BACKGROUND.activate(this.sun);
                Scene3D.BACKGROUND.activate(this.moon);
            }
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
        } else if (wasSunMoonShown) {
            Scene3D.BACKGROUND.deactivate(this.sun);
            Scene3D.BACKGROUND.deactivate(this.moon);
        }
    }

    public void remove(ClientChunk clientChunk) {
        this.removedChunks.add(clientChunk);
    }

    public void addParticles(ParticleEffect obtained, Vec3d position, Vec3d motion, int count) {
        LocalPlayer player = client.player;
        if (player == null) return;
        Vec3f div = position.sub(player.getPosition(client.partialTick).add(0, player.getEyeHeight(), 0)).f().div(WorldRenderer.SCALE);

        Vector3 vector3 = new Vector3(div.x, div.y, div.z);
        obtained.translate(vector3);
        particleSystem.add(obtained);
    }

    private record MeshMaterial(Mesh mesh, Material material) {

    }

    private static class ChunkRenderRef {
        boolean chunkRendered = false;
    }
}
