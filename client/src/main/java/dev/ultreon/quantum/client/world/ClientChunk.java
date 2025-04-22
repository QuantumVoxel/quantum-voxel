package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientChunkEvents;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.registry.BlockEntityModelRegistry;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.render.NodeCategory;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.render.meshing.GreedyMesher;
import dev.ultreon.quantum.client.render.meshing.Mesher;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.network.packets.c2s.C2SChunkStatusPacket;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.util.PosOutOfBoundsException;
import dev.ultreon.quantum.util.ShowInNodeView;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import static dev.ultreon.quantum.world.World.CS;
import static dev.ultreon.quantum.world.World.CS_2;

public final class ClientChunk extends Chunk implements ClientChunkAccess {
    private static final int[] dx = {-1, 0, 1, 0, 0, 0};
    private static final int[] dy = {0, -1, 0, 1, 0, 0};
    private static final int[] dz = {0, 0, 0, 0, -1, 1};

    final Mesher mesher;
    private final ClientWorld clientWorld;
    public final Vector3 renderOffset = new Vector3();
    public final Vector3 deltaOffset = new Vector3();

    public volatile boolean dirty;
    public boolean initialized = false;
    private final QuantumClient client = QuantumClient.get();
    private final Map<BlockVec, BlockState> customRendered = new HashMap<>();
    public boolean immediateRebuild = false;
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp1 = new Vector3();
    private final Map<BlockVec, ModelInstance> addedModels = new ConcurrentHashMap<>();
    private final Map<BlockVec, BlockObject> models = new ConcurrentHashMap<>();
    private final Array<BlockVec> removedModels = new Array<>();
    private final ObjectMap<Vec3i, LightSource> lights = new ObjectMap<>();
    private final Stack<Integer> stack = new Stack<>();
    public final ClientChunkInfo info = new ClientChunkInfo();
    public int lod;
    public MeshStatus meshStatus = MeshStatus.UNMESHED;
    public long meshDuration = 0L;
    public String meshLog = "";
    public int vertexCount;
    public int indexCount;
    public int faceCount;
    public int meshVertices;
    private boolean empty = false;
    private final BoundingBox boundingBox = new BoundingBox();

    @ShowInNodeView
    private final ObjectMap<RenderPass, ChunkMesh> meshes = new ObjectMap<>();
    private final int[] opqueColumnMask = new int[CS_2];

    /**
     * @deprecated Use {@link #ClientChunk(ClientWorld, dev.ultreon.quantum.world.vec.ChunkVec, Storage, Storage, Map)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public ClientChunk(ClientWorld world, int ignoredSize, int ignoredHeight, ChunkVec pos, Storage<BlockState> storage, @NotNull Storage<RegistryKey<Biome>> biomeStorage, Map<BlockVec, BlockEntityType<?>> blockEntities) {
        this(world, pos, storage, biomeStorage, blockEntities);
    }

    public ClientChunk(ClientWorld world, ChunkVec pos, Storage<BlockState> storage, @NotNull Storage<RegistryKey<Biome>> biomeStorage, Map<BlockVec, BlockEntityType<?>> blockEntities) {
        super(world, pos, storage, biomeStorage);
        this.clientWorld = world;
        this.active = false;

        blockEntities.forEach((vec, type) -> {
            if (type != null) {
                this.setBlockEntity(vec, type.create(world, pos.blockInWorldSpace(vec)));
            }
        });

        this.mesher = new GreedyMesher(this, false);

        for (int i = 0; i < CS; i++) {
            for (int j = 0; j < CS; j++) {
                int opque = 0;
                int index = index(i, j);
                for (int k = 0; k < CS; k++) {
                    BlockState state = this.storage.get(index);
                    if (!state.isAir() && !state.isInvisible()) {
                        opque |= 1 << k;
                    }
                }

                this.opqueColumnMask[index] = opque;
            }
        }
    }

    public int index(int x, int z) {
        return (z * CS) + x;
    }

    public int index(int x, int y, int z) {
        return (z * CS + y) * CS + x;
    }

    @Override
    public float getLightLevel(int x, int y, int z) throws PosOutOfBoundsException {
        if(this.isOutOfBounds(x, y, z))
            throw new PosOutOfBoundsException();

        int sunlight = this.lightMap.getSunlight(x, y, z);
        int blockLight = this.lightMap.getBlockLight(x, y, z);
        float sunlightMapped = Chunk.lightLevelMap[Mth.clamp(sunlight, 0, Chunk.MAX_LIGHT_LEVEL)] - Chunk.lightLevelMap[0];
        float blockLightMapped = Chunk.lightLevelMap[Mth.clamp(blockLight, 0, Chunk.MAX_LIGHT_LEVEL)] - Chunk.lightLevelMap[0];

        return Mth.clamp(sunlightMapped + blockLightMapped + Chunk.lightLevelMap[0], Chunk.lightLevelMap[0], Chunk.lightLevelMap[Chunk.MAX_LIGHT_LEVEL]);
    }

    @Override
    public float getSunlightLevel(int x, int y, int z) {
        int sunlight = this.getSunlight(x, y, z);
        float sunlightMapped = Chunk.lightLevelMap[Mth.clamp(sunlight, 0, Chunk.MAX_LIGHT_LEVEL)] - Chunk.lightLevelMap[0];
        
        return Mth.clamp(sunlightMapped + Chunk.lightLevelMap[0], Chunk.lightLevelMap[0], Chunk.lightLevelMap[Chunk.MAX_LIGHT_LEVEL]);
    }

    @Override
    public float getBlockLightLevel(int x, int y, int z) {
        int blockLight = this.getBlockLight(x, y, z);
        float blockLightMapped = Chunk.lightLevelMap[Mth.clamp(blockLight, 0, Chunk.MAX_LIGHT_LEVEL)] - Chunk.lightLevelMap[0];
        
        return Mth.clamp(blockLightMapped + Chunk.lightLevelMap[0], Chunk.lightLevelMap[0], Chunk.lightLevelMap[Chunk.MAX_LIGHT_LEVEL]);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void revalidate() {
        this.initialized = false;
    }

    @Override
    public void dispose() {
        if (!QuantumClient.isOnRenderThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        synchronized (this) {
            super.dispose();

            @Nullable TerrainRenderer worldRenderer = QuantumClient.get().worldRenderer;
            if (worldRenderer != null) worldRenderer.unload(this);

            this.tmp.setZero();
            this.tmp1.setZero();

            if (this.client.connection != null) {
                this.client.connection.send(new C2SChunkStatusPacket(this.vec, Chunk.Status.UNLOADED));
            }
        }
    }

    @Override
    protected @NotNull BlockState getFast(int x, int y, int z) {
        return super.getFast(x, y, z);
    }

    @Override
    protected void setFast(Vec3i pos, BlockState block) {
        if (!QuantumClient.isOnRenderThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        super.setFast(pos, block);
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        if (!QuantumClient.isOnRenderThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        return super.set(x, y, z, block);
    }

    @Override
    protected boolean setFast(int x, int y, int z, BlockState block) {
        if (!QuantumClient.isOnRenderThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        this.removedModels.add(new BlockVec(x, y, z, BlockVecSpace.CHUNK));

        boolean isBlockSet = super.setFast(x, y, z, block);

        this.dirty = true;
        this.immediateRebuild = true;
        this.clientWorld.updateChunkAndNeighbours(this);

        if (!block.isAir())
            this.empty = false;

        return isBlockSet;
    }

    @Override
    public void onUpdated() {
        if (!QuantumClient.isOnRenderThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        super.onUpdated();

        ClientChunkEvents.REBUILT.factory().onClientChunkRebuilt(this);
    }

    @Override
    public @NotNull ClientWorld getWorld() {
        return this.clientWorld;
    }

    @Override
    public Vector3 getRenderOffset() {
        return renderOffset;
    }

    void ready() {
        this.ready = true;
        this.clientWorld.updateChunkAndNeighbours(this);

        ClientChunkEvents.BUILT.factory().onClientChunkRebuilt(this);
    }

    public Map<BlockVec, BlockState> getCustomRendered() {
        return this.customRendered;
    }

    @Override
    protected void setBlockEntity(BlockVec blockVec, BlockEntity blockEntity) {
        super.setBlockEntity(blockVec, blockEntity);

        System.out.println("BlockVec = " + blockVec);

        BlockModel blockModel = BlockEntityModelRegistry.get(blockEntity.getType());
        if (blockModel != null) {
            blockModel.loadInto(blockEntity.pos(), this);
        } else {
            QuantumClient.LOGGER.warn("No block entity model for {} at {}", blockEntity.getType().getId(), blockVec);
        }
    }

    public void addModel(BlockVec pos, ModelInstance instance) {
        if (models.containsKey(pos)) {
            BlockObject modelInstance1 = this.models.get(pos);
            this.remove(modelInstance1);
            this.models.remove(pos);
        }
        this.addedModels.put(pos, instance);
    }

    @Override
    public boolean isLoaded() {
        return this.world.isLoaded(this);
    }

    @Override
    public void markEmpty() {
        this.empty = true;
    }

    @Override
    public void markNotEmpty() {
        this.empty = false;
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public BlockState getSafe(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= CS || y >= CS || z >= CS) {
            BlockVec start = vec.start();
            x += start.x;
            y += start.y;
            z += start.z;

            return clientWorld.get(x, y, z);
        }

        return get(x, y, z);
    }

    @Override
    public int getBlockLightSafe(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= CS || y >= CS || z >= CS) {
            BlockVec start = vec.start();
            x += start.x;
            y += start.y;
            z += start.z;

            return clientWorld.getBlockLight(x, y, z);
        }

        return getBlockLight(x, y, z);
    }

    @Override
    public int getSunlightSafe(int x, int y, int z) {
        return !getSafe(x, y, z).isAir() ? 15 : 6;
    }

    public void renderModels(NodeCategory nodeCategory) {
        for (BlockVec pos : this.addedModels.keySet()) {
            ModelInstance model = this.addedModels.get(pos);
            model.userData = Shaders.MODEL_VIEW.get();
            this.addedModels.remove(pos);
            BlockObject value = new BlockObject(model);
            this.models.put(pos, value);
            nodeCategory.add("Block Object", value);
        }

        for (BlockVec pos : this.models.keySet()) {
            BlockObject inst = this.models.get(pos);
            inst.transform.setToTranslationAndScaling(renderOffset.x + pos.getIntX(), renderOffset.y + pos.getIntY(), renderOffset.z + pos.getIntZ(), 1 / 16f, 1 / 16f, 1 / 16f);
        }

        for (BlockVec pos : this.removedModels.toArray(BlockVec.class)) {
            this.removedModels.removeValue(pos, false);
            BlockObject model = this.models.remove(pos);
            if (model != null)
                this.remove(model);
        }
    }

    public void loadCustomRendered() {
        for (BlockEntity blockEntity : getBlockEntities()) {
            BlockModel blockModel = BlockEntityModelRegistry.get(blockEntity.getType());
            if (blockModel != null) {
                blockModel.loadInto(blockEntity.pos(), this);
            }
        }
    }

    @ApiStatus.Internal
    public void whileLocked(Runnable func) {
        if (!QuantumClient.isOnRenderThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        synchronized (this) {
            func.run();
        }
    }

    public QuantumClient getClient() {
        return client;
    }

    public void destroyModels() {
        for (var model : this.models.values()) {
            this.remove(model);
        }
    }

    public void setBlockLight(int x, int y, int z, int level) {
        this.lightMap.setBlockLight(x, y, z, level);
    }

    public void setBlockLight(Vec3i pos, int light) {
        this.setBlockLight(pos.x, pos.y, pos.z, light);
    }

    public void updateLight(WorldAccess world) {
        world.updateLightSources(this.getOffset(), lights);
    }

    public void setLightSource(Vec3i tmp, int light) {
        if (light == 0) {
            this.lights.remove(tmp);
        }

        this.lights.put(tmp.cpy(), new LightSource(tmp.x, tmp.y, tmp.z, light));
    }

    public void clearLight() {
        this.lightMap.clear();
    }

    public void setSunlight(BlockVec pos, int intensity) {
        lightMap.setSunlight(pos.getIntX(), pos.getIntY(), pos.getIntZ(), intensity);
    }

    public void floodFill(int startX, int startY, int startZ, byte newValue) {
        byte oldValue = lightMap.getBlockLight(index(startX, startY, startZ));
        if (oldValue == newValue) return;

        stack.clear();
        stack.push(index(startX, startY, startZ));

        while (!stack.isEmpty()) {
            int idx = stack.pop();
            if (lightMap.getBlockLight(idx) != oldValue) continue;

            lightMap.setBlockLight(idx, newValue);
            int x = idx % CS;
            int y = (idx / CS) % CS;
            int z = idx / (CS * CS);
            for (int i = 0; i < 6; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                int nz = z + dz[i];
                if (nx >= 0 && nx < CS && ny >= 0 && ny < CS && nz >= 0 && nz < CS) {
                    int lightReduction = get(nx, ny, nz).getLightReduction();
                    if (lightReduction == 0) continue;
                    if (lightMap.getSunlight(nx, ny, nz) > lightReduction) continue;
                    if (lightMap.getBlockLight(index(nx, ny, nz)) != oldValue) continue;
                    stack.push(index(nx, ny, nz));
                }
            }
        }
    }

    public int getSunlight(BlockVec pos) {
//        return lightMap.getSunlight(pos.getIntX(), pos.getIntY(), pos.getIntZ());
        return getSunlight(pos.getIntX(), pos.getIntY(), pos.getIntZ());
    }

    public int getSunlight(int x, int y, int z) {
//        return get(x, y, z).isAir() ? 15 : 0;
        return 15;
    }

    public BoundingBox getBoundingBox() {
        this.boundingBox.min.set(renderOffset).sub(WorldRenderer.HALF_CHUNK_DIMENSIONS);
        this.boundingBox.max.set(renderOffset).add(WorldRenderer.HALF_CHUNK_DIMENSIONS);

        return this.boundingBox;
    }

    public void addMesh(ChunkMesh chunkMesh) {
        this.meshes.put(chunkMesh.pass, chunkMesh);
        this.add("Mesh " + chunkMesh.pass.name(), chunkMesh);
    }

    public void removeMesh(ChunkMesh chunkMesh) {
        this.meshes.remove(chunkMesh.pass);
        this.remove(chunkMesh);
    }

    public int[] getOpaqueMask() {
        return opqueColumnMask;
    }

    public void render(RenderBufferSource source) {
        for (ChunkMesh chunkMesh : this.meshes.values()) {
            chunkMesh.render(client.camera, source);
        }
    }
}
