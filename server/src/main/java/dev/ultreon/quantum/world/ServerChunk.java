package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.events.WorldEvents;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.S2CChunkDataPacket;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.ubo.DataTypes;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;
import java.util.List;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;
import static dev.ultreon.quantum.world.World.LOGGER;
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
    private boolean modified = false;
    private boolean original = true;

    private final @NotNull PlayerTracker tracker = new PlayerTracker();
    private long lastTracked = currentTimeMillis();
    private long trackDuration = 10000L;

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
            LOGGER.debug(String.format("Loading chunk at %s", pos));
        }

        var storage = new PaletteStorage<>(CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE, Blocks.AIR.getDefaultState());
        var biomeStorage = new PaletteStorage<>(CHUNK_SIZE * CHUNK_SIZE, world.getServer().getBiomes().getDefaultKey());

        MapType blockData = chunkData.getMap("Blocks");
        storage.load(blockData, BlockState::load);

        MapType biomeData = chunkData.getMap("Biomes");
        biomeStorage.load(biomeData, data -> RegistryKey.of(RegistryKeys.BIOME, new NamespaceID(data.getString("id"))));

        ServerChunk chunk = new ServerChunk(world, pos, storage, biomeStorage, region);
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

            return result;
        }
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
                    BlockVec blockVec = new BlockVec(data.getInt("x"), data.getInt("y"), data.getInt("z"), BlockVecSpace.WORLD);
                    BlockEntity blockEntity = BlockEntity.fullyLoad(world, blockVec, data);
                    this.setBlockEntity(blockVec.chunkLocal(), blockEntity);
                }
            }

            WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
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

            WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
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

    public boolean shouldSave() {
        return modified && ready;
    }

    public boolean isOriginal() {
        return original;
    }

    public @NotNull PlayerTracker getTracker() {
        return this.tracker;
    }

    public void sendChunk() {
        if (!isBeingTracked() && lastTracked + trackDuration < System.currentTimeMillis()) {
            this.world.unloadChunk(this, this.getVec());
            return;
        }

        this.world.getServer().onChunkSent(this);
        this.sendAllViewers(new S2CChunkDataPacket(this.getVec(), this.info, this.storage.clone(), this.biomeStorage.clone(), this.getBlockEntities()));

    }

    public void tick() {
        Collection<BlockEntity> blockEntities;
        synchronized (this){
            if (!isBeingTracked() && lastTracked + trackDuration < System.currentTimeMillis()) {
                this.world.unloadChunk(this, this.getVec());
                return;
            } else if (isBeingTracked()) {
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
}
