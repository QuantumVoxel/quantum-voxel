package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientChunkEvents;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.registry.BlockEntityModelRegistry;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.render.meshing.GreedyMesher;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.network.packets.c2s.C2SChunkStatusPacket;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.util.PosOutOfBoundsException;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.ChunkPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientChunk extends Chunk {
    public static final RenderablePool RENDERABLE_POOL = new RenderablePool();
    final GreedyMesher mesher;
    private final ClientWorld clientWorld;
    public final Vector3 renderOffset = new Vector3();

    public Model model;
    public ModelInstance modelInstance;

    public volatile boolean dirty;
    public boolean initialized = false;
    private final QuantumClient client = QuantumClient.get();
    private final Map<BlockPos, BlockProperties> customRendered = new HashMap<>();
    public boolean immediateRebuild = false;
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp1 = new Vector3();
    private final Map<BlockPos, ModelInstance> addedModels = new ConcurrentHashMap<>();
    private final Map<BlockPos, ModelInstance> models = new ConcurrentHashMap<>();
    private final Array<BlockPos> removedModels = new Array<>();
    public boolean visible;

    /**
     * @deprecated Use {@link #ClientChunk(ClientWorld, ChunkPos, Storage, Storage, Map)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public ClientChunk(ClientWorld world, int ignoredSize, int ignoredHeight, ChunkPos pos, Storage<BlockProperties> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities) {
        this(world, pos, storage, biomeStorage, blockEntities);
    }

    public ClientChunk(ClientWorld world, ChunkPos pos, Storage<BlockProperties> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities) {
        super(world, pos, storage, biomeStorage);
        this.clientWorld = world;
        this.active = false;

        blockEntities.forEach((blockPos, type) -> {
            if (type != null) {
                this.setBlockEntity(blockPos, type.create(world, blockPos.offset(pos)));
            }
        });

        this.mesher = new GreedyMesher(this, true);
    }

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
    public void dispose() {
        if (!QuantumClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }


        synchronized (this) {
            super.dispose();

            WorldRenderer worldRenderer = QuantumClient.get().worldRenderer;
            if (this.model != null && this.modelInstance != null) {
                if (worldRenderer != null) {
                    worldRenderer.unload(this);
                }
            }

            this.tmp.setZero();
            this.tmp1.setZero();

            this.client.connection.send(new C2SChunkStatusPacket(this.getPos(), Chunk.Status.UNLOADED));
        }
    }

    @Override
    public BlockProperties getFast(int x, int y, int z) {
        if (!QuantumClient.isOnMainThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        return super.getFast(x, y, z);
    }

    @Override
    public void setFast(Vec3i pos, BlockProperties block) {
        if (!QuantumClient.isOnMainThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        super.setFast(pos, block);
    }

    @Override
    public boolean set(int x, int y, int z, BlockProperties block) {
        if (!QuantumClient.isOnMainThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        return super.set(x, y, z, block);
    }

    @Override
    public void set(Vec3i pos, BlockProperties block) {
        if (!QuantumClient.isOnMainThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        super.set(pos, block);
    }

    @Override
    public boolean setFast(int x, int y, int z, BlockProperties block) {
        if (!QuantumClient.isOnMainThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        this.removedModels.add(new BlockPos(x, y, z));

        boolean isBlockSet = super.setFast(x, y, z, block);

        this.dirty = true;
        this.clientWorld.updateChunkAndNeighbours(this);
        return isBlockSet;
    }

    public void updated() {
        this.dirty = false;
    }

    @Override
    public void onUpdated() {
        if (!QuantumClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        super.onUpdated();

        ClientChunkEvents.REBUILT.factory().onClientChunkRebuilt(this);
    }

    @Override
    public ClientWorld getWorld() {
        return this.clientWorld;
    }

    void ready() {
        if (!QuantumClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }
        this.ready = true;
        this.clientWorld.updateChunkAndNeighbours(this);

        ClientChunkEvents.BUILT.factory().onClientChunkRebuilt(this);
    }

    public Object getBounds() {
        return null;
    }

    public Map<BlockPos, BlockProperties> getCustomRendered() {
        return this.customRendered;
    }

    @Override
    protected void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
        super.setBlockEntity(blockPos, blockEntity);

        System.out.println("blockPos = " + blockPos);

        BlockModel blockModel = BlockEntityModelRegistry.get(blockEntity.getType());
        if (blockModel != null) {
            blockModel.loadInto(blockEntity.pos(), this);
        } else {
            QuantumClient.LOGGER.warn("No block entity model for " + blockEntity.getType().getId() + " at " + blockPos);
        }
    }

    @CanIgnoreReturnValue
    public ModelInstance addModel(BlockPos pos, ModelInstance instance) {
        if (models.containsKey(pos)) {
            ModelInstance modelInstance1 = this.models.get(pos);
            RenderLayer.WORLD.destroy(modelInstance1);
            this.models.remove(pos);
        }
        return this.addedModels.put(pos, instance);
    }

    public void renderModels(RenderLayer renderLayer) {
        for (BlockPos pos : this.addedModels.keySet()) {
            ModelInstance model = this.addedModels.get(pos);
            model.userData = Shaders.MODEL_VIEW.get();
            this.addedModels.remove(pos);
            this.models.put(pos, model);
            renderLayer.add(model);
        }

        for (BlockPos pos : this.models.keySet()) {
            ModelInstance inst = this.models.get(pos);
            inst.transform.setToTranslationAndScaling(renderOffset.x + pos.x(), renderOffset.y + pos.y(), renderOffset.z + pos.z(), 1 / 16f, 1 / 16f, 1 / 16f);
        }

        for (BlockPos pos : this.removedModels) {
            this.removedModels.removeValue(pos, false);
            ModelInstance model = this.models.remove(pos);
            if (model != null)
                renderLayer.destroy(model);
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
        if (!QuantumClient.isOnMainThread()) {
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
            RenderLayer.WORLD.destroy(model);
        }
    }
}
