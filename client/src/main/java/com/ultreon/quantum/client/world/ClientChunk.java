package com.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.block.entity.BlockEntity;
import com.ultreon.quantum.block.entity.BlockEntityType;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.api.events.ClientChunkEvents;
import com.ultreon.quantum.client.init.Shaders;
import com.ultreon.quantum.client.model.block.BlockModel;
import com.ultreon.quantum.client.registry.BlockEntityModelRegistry;
import com.ultreon.quantum.client.render.ModelObject;
import com.ultreon.quantum.client.render.meshing.GreedyMesher;
import com.ultreon.quantum.client.util.RenderableArray;
import com.ultreon.quantum.collection.Storage;
import com.ultreon.quantum.network.packets.c2s.C2SChunkStatusPacket;
import com.ultreon.quantum.util.InvalidThreadException;
import com.ultreon.quantum.util.PosOutOfBoundsException;
import com.ultreon.quantum.world.Biome;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.Chunk;
import com.ultreon.quantum.world.ChunkPos;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientChunk extends Chunk {
    public static final RenderablePool RENDERABLE_POOL = new RenderablePool();
    final GreedyMesher mesher;
    private final ClientWorld clientWorld;
    public final Vector3 renderOffset = new Vector3();
    public ChunkMesh solidMesh;
    public ChunkMesh transparentMesh;
    public volatile boolean dirty;
    public boolean initialized = false;
    private final QuantumClient client = QuantumClient.get();
    private final Map<BlockPos, BlockProperties> customRendered = new HashMap<>();
    private final Map<BlockPos, ModelInstance> models = new HashMap<>();
    public boolean immediateRebuild = false;
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp1 = new Vector3();
    private final List<ModelObject> modelObjects = new ArrayList<>();

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

            for (ModelObject modelObject : this.modelObjects) {
                modelObject.dispose();
            }

            WorldRenderer worldRenderer = QuantumClient.get().worldRenderer;
            if ((this.solidMesh != null || this.transparentMesh != null) && worldRenderer != null) {
                worldRenderer.free(this);
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

        this.models.remove(new BlockPos(x, y, z));

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
        return this.models.put(pos, instance);
    }

    public void renderModels(Array<Renderable> output, Pool<Renderable> pool) {
        for (Map.Entry<BlockPos, ModelInstance> entry : this.models.entrySet()) {
            ModelInstance model = entry.getValue();
            if (model == null) continue;

            BlockPos key = entry.getKey();

            float x = (float) key.x() % 16;
            float z = (float) key.z() % 16;
            if (x < 0) x += 16;
            if (z < 0) z += 16;
            ModelObject modelObject = model.userData instanceof ModelObject ? (ModelObject) model.userData : null;
            if (modelObject == null) {
                RenderableArray renderables = new RenderableArray();
                model.getRenderables(renderables, RENDERABLE_POOL);
                model.userData = modelObject = new ModelObject(Shaders.MODEL_VIEW.get(), model, renderables);
            }
            modelObject.renderables().clear();
            model.transform.setToTranslationAndScaling(this.renderOffset.x + x, this.renderOffset.y + (float) key.y() % 65536, this.renderOffset.z + z, 1 / 16f, 1 / 16f, 1 / 16f);
            model.getRenderables(modelObject.renderables(), RENDERABLE_POOL);
            output.addAll(modelObject.renderables());

            this.modelObjects.add(modelObject);
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
}
