package dev.ultreon.quantum.world;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CChunkDataPacket;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.ubo.DataTypes;
import dev.ultreon.quantum.ubo.types.ListType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.ultreon.quantum.world.World.*;
import static java.lang.System.currentTimeMillis;

/**
 * The {@code ServerChunk} class represents a chunk of the game world in a server environment.
 * It extends the {@code Chunk} class, providing additional server-specific functionality and is not thread-safe.
 * This class manages the state and behavior of a chunk, including loading, saving, and tracking modifications.
 */
@NotThreadSafe
public final class ServerChunk extends Chunk {
    private final @NotNull ServerWorld world;
    private final @NotNull ServerWorld.Region region;
    public final ChunkBuildInfo info = new ChunkBuildInfo();
    protected boolean modified = false;
    protected boolean original = true;

    private final @NotNull PlayerTracker tracker = new PlayerTracker();
    private long lastTracked = currentTimeMillis();
    private final long trackDuration = 10000L;
    private final AtomicInteger rTick = new AtomicInteger(0);
    private boolean scheduledSend = false;

    public ServerChunk(@NotNull ServerWorld world,
                       @NotNull ChunkVec pos,
                       @NotNull Storage<BlockState> storage,
                       @NotNull Storage<RegistryKey<Biome>> biomeStorage,
                       @NotNull ServerWorld.Region region) {
        super(world, pos, storage, biomeStorage);
        this.world = world;
        this.region = region;
    }

    public static ServerChunk load(@NotNull ServerWorld world,
                                   @NotNull ChunkVec pos,
                                   @NotNull MapType chunkData,
                                   @NotNull ServerWorld.Region region) {

        if (DebugFlags.CHUNK_LOADER_DEBUG.isEnabled()) {
            LOGGER.debug("Loading chunk at {}", pos);
        }

        var storage = new PaletteStorage<>(CS_3, Blocks.AIR.getDefaultState());
        var biomeStorage = new PaletteStorage<>(CS_2, world.getServer().getBiomes().getDefaultKey());

        MapType blockData = chunkData.getMap("Blocks");
        storage.load(blockData, BlockState::load);

        MapType biomeData = chunkData.getMap("Biomes");
        biomeStorage.load(biomeData, data -> RegistryKey.of(RegistryKeys.BIOME, new NamespaceID(data.getString("id"))));

        ServerChunk chunk = new ServerChunk(world, pos, storage, biomeStorage, region);
        chunk.active = true;
        chunk.modified = false;
        chunk.original = false;
        chunk.lastTracked = currentTimeMillis();
        chunk.load(chunkData);
        return chunk;
    }

    @Override
    protected boolean setFast(int x,
                              int y,
                              int z,
                              @NotNull BlockState block) {
        synchronized (this) {
            this.region.markDirty();
            boolean result = super.setFast(x, y, z, block);

            if (result) {
                this.modified = true;
                this.original = false;
            }

            this.scheduledSend = true;

            return result;
        }
    }

    @Override
    protected void setFast(Vec3i pos, BlockState block) {
        setFast(pos.x, pos.y, pos.z, block);
    }

    public void load(@NotNull MapType chunkData) {
        synchronized (this) {
            MapType extra = chunkData.getMap("Extra", new MapType());
            this.original = chunkData.getBoolean("original", this.original);

            if (chunkData.contains("LightMap", DataTypes.BYTE_ARRAY))
                this.lightMap.load(chunkData.getByteArray("LightMap"));

            this.modified = false;

            if (chunkData.contains("BlockEntities", DataTypes.LIST)) {
                ListType<MapType> blockEntities = chunkData.getList("BlockEntities");

                for (MapType data : blockEntities.getValue()) {
                    BlockVec blockVec = new BlockVec(data.getInt("x"), data.getInt("y"), data.getInt("z"));
                    BlockEntity blockEntity = BlockEntity.fullyLoad(world, blockVec, data);
                    if (blockEntity != null)
                        this.setBlockEntity(blockVec.chunkLocal(), blockEntity);
                }
            }

//            WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
        }
    }

    @Override
    protected void setBlockEntity(@NotNull BlockVec blockVec,
                                  @NotNull BlockEntity blockEntity) {
        synchronized (this) {
            super.setBlockEntity(blockVec, blockEntity);
        }
    }

    public void sendAllViewers(@NotNull Packet<? extends @NotNull ClientPacketHandler> packet) {
        this.tracker.sendPacket(packet);
    }

    public boolean isBeingTracked() {
        return this.tracker.isAnyoneTracking();
    }

    public MapType save() {
        if (!QuantumServer.isOnServerThread()) {
            return QuantumServer.invokeAndWait(this::save);
        }

        MapType data = new MapType();
        MapType chunkData = new MapType();
        MapType biomeData = new MapType();
        ListType<MapType> blockEntitiesData = new ListType<>();
        MapType extra = new MapType();

        synchronized (this) {
            this.storage.save(chunkData, BlockState::save);
            this.biomeStorage.save(biomeData, biomeRegistryKey -> {
                MapType biomeUbo = new MapType();

                biomeUbo.putString("id", biomeRegistryKey.id().toString());
                return biomeUbo;
            });

            for (BlockEntity blockEntity : this.getBlockEntities()) {
                MapType blockEntityData = new MapType();
                blockEntity.save(blockEntityData);

                blockEntitiesData.add(blockEntityData);
            }
            data.put("BlockEntities", blockEntitiesData);
            data.put("Biomes", biomeData);
            data.put("Blocks", chunkData);
            data.putByteArray("LightMap", this.lightMap.save());
            data.putBoolean("original", this.original);

//            WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
            if (!extra.getValue().isEmpty()) {
                data.put("Extra", extra);
            }

            this.modified = false;
        }

        return data;
    }

    @Override
    public @NotNull ServerWorld getWorld() {
        return this.world;
    }

    @Override
    public void reset() {
        this.storage.setUniform(Blocks.AIR.getDefaultState());
        sendChunk();
    }

    public boolean isOriginal() {
        return original;
    }

    public @NotNull PlayerTracker getTracker() {
        return this.tracker;
    }

    public void sendChunk() {
        scheduledSend = false;
        if (!isBeingTracked() && lastTracked + trackDuration < System.currentTimeMillis()) {
            this.world.unloadChunk(this, this.vec);
            return;
        }

        this.world.getServer().onChunkSent(this);
        this.sendAllViewers(new S2CChunkDataPacket(this.vec, this.info, this.storage.clone(), this.biomeStorage.clone(), this.getBlockEntities()));

    }

    public void tick() {
        Collection<BlockEntity> blockEntities;

        synchronized (this){
            if (!isBeingTracked() && lastTracked + trackDuration < System.currentTimeMillis()) {
                this.world.unloadChunk(this, this.vec);
                return;
            } else if (isBeingTracked()) {
                if (scheduledSend) this.sendChunk();

                lastTracked = System.currentTimeMillis();
            }

            blockEntities = List.copyOf(this.getBlockEntities());
        }

        for (BlockEntity blockEntity : blockEntities) {
            blockEntity.tick();
        }
    }

    public void stopTracking(ServerPlayer serverPlayer) {
        this.tracker.stopTracking(serverPlayer);
    }

    public void randomTick() {
        int randX = CommonConstants.RANDOM.nextInt(CS);
        int randY = CommonConstants.RANDOM.nextInt(CS);
        int randZ = CommonConstants.RANDOM.nextInt(CS);

        BlockState blockState = this.get(randX, randY, randZ);
        if (!blockState.doesRandomTick())
            return;

        blockState.randomTick(this, vec.blockInWorldSpace(new BlockVec(randX, randY, randZ)));
    }

    public boolean isEmpty() {
        return this.storage.isUniform() && this.storage.get(0).isAir();
    }
}
