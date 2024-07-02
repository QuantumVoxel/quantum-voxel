package dev.ultreon.quantum.world;

import dev.ultreon.ubo.types.ByteArrayType;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.ubo.types.ShortArrayType;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockData;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.events.WorldEvents;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.world.gen.biome.Biomes;

import javax.annotation.concurrent.NotThreadSafe;

import static dev.ultreon.quantum.world.World.CHUNK_HEIGHT;
import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

@NotThreadSafe
public final class ServerChunk extends Chunk {
    private final ServerWorld world;
    private final ServerWorld.Region region;
    private boolean modified = false;
    private boolean original = true;
    private boolean locked = false;

    public ServerChunk(ServerWorld world, ChunkPos pos, Storage<BlockData> storage, Storage<Biome> biomeStorage, ServerWorld.Region region) {
        super(world, pos, storage, biomeStorage);
        this.world = world;
        this.region = region;
    }

    @Override
    public boolean setFast(int x, int y, int z, BlockData block) {
        if (!QuantumServer.isOnServerThread()) {
            throw new InvalidThreadException("Should be on server thread.");
        }

        if (this.locked) return false;

        Boolean result = this.region.trySet(() -> {
            this.region.markDirty();
            return super.setFast(x, y, z, block);
        }).getValueOr(false);


        if (result) {
            this.modified = true;
            this.original = false;
        }
        return result;
    }

    public static ServerChunk load(ServerWorld world, ChunkPos pos, MapType chunkData, ServerWorld.Region region) {
        var storage = new PaletteStorage<>(BlockData.AIR, CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE);
        var biomeStorage = new PaletteStorage<>(Biomes.PLAINS, CHUNK_SIZE * CHUNK_SIZE);

        MapType blockData = chunkData.getMap("Blocks");
        storage.load(blockData, BlockData::load);

        MapType biomeData = chunkData.getMap("Biomes");
        biomeStorage.load(biomeData, Biome::load);

        ServerChunk chunk = new ServerChunk(world, pos, storage, biomeStorage, region);
        chunk.load(chunkData);
        return chunk;
    }

    public void load(MapType chunkData) {
        MapType extra = chunkData.getMap("Extra", new MapType());
        this.original = chunkData.getBoolean("original", this.original);

        if (chunkData.<ShortArrayType>contains("HeightMap")) {
            this.heightMap.load(chunkData.getShortArray("HeightMap"));
        }

        if (chunkData.<ByteArrayType>contains("LightMap")) {
            this.lightMap.load(chunkData.getByteArray("LightMap"));
        }

        this.modified = false;

        if (chunkData.<ListType<?>>contains("BlockEntities")) {
            ListType<MapType> blockEntities = chunkData.getList("BlockEntities");

            for (MapType data : blockEntities.getValue()) {
                BlockPos blockPos = new BlockPos(data.getInt("x"), data.getInt("y"), data.getInt("z"));
                BlockEntity blockEntity = BlockEntity.fullyLoad(world, blockPos, data);
                this.setBlockEntity(World.toLocalBlockPos(blockPos), blockEntity);
            }
        }

        WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
    }

    protected void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
        if (!QuantumServer.isOnServerThread()) {
            QuantumServer.invokeAndWait(() -> setBlockEntity(blockPos, blockEntity));
            return;
        }

        if (this.locked) return;

        super.setBlockEntity(blockPos, blockEntity);
    }

    public MapType save() {
        if (!QuantumServer.isOnServerThread()) {
            return QuantumServer.invokeAndWait(this::save);
        }

        this.locked = true;

        MapType data = new MapType();
        MapType chunkData = new MapType();
        MapType biomeData = new MapType();
        ListType<MapType> blockEntitiesData = new ListType<>();

        this.storage.save(chunkData, BlockData::save);
        this.biomeStorage.save(biomeData, Biome::save);

        for (BlockEntity blockEntity : this.getBlockEntities()) {
            MapType blockEntityData = new MapType();
            blockEntity.save(blockEntityData);

            blockEntitiesData.add(blockEntityData);
        }
        data.put("BlockEntities", blockEntitiesData);
        data.put("Biomes", biomeData);
        data.put("Blocks", chunkData);
        data.putShortArray("HeightMap", this.heightMap.save());
        data.putByteArray("LightMap", this.lightMap.save());
        data.putBoolean("original", this.original);

        MapType extra = new MapType();
        WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
        if (!extra.getValue().isEmpty()) {
            data.put("Extra", extra);
        }

        this.modified = false;
        this.locked = false;

        return data;
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean shouldSave() {
        return modified && ready;
    }

    public boolean isOriginal() {
        return original;
    }
}
