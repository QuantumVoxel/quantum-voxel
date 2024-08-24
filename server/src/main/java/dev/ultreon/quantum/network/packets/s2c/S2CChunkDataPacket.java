package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.ChunkBuildInfo;
import dev.ultreon.quantum.world.gen.biome.Biomes;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class S2CChunkDataPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkVec pos;
    private final ChunkBuildInfo info;
    private final Storage<BlockState> storage;
    private final Storage<Biome> biomeStorage;
    private final IntList blockEntityPositions = new IntArrayList();
    private final IntList blockEntities = new IntArrayList();
    public static final int MAX_SIZE = 1048576;

    public S2CChunkDataPacket(PacketIO buffer) {
        this.pos = buffer.readChunkVec();
        this.info = new ChunkBuildInfo(buffer);
        this.storage = new PaletteStorage<>(BlockState.AIR, buffer, PacketIO::readBlockMeta);
        this.biomeStorage = new PaletteStorage<>(Biomes.PLAINS, buffer, buf -> Registries.BIOME.byId(buf.readShort()));

        int blockEntityCount = buffer.readVarInt();
        for (int i = 0; i < blockEntityCount; i++) {
            blockEntityPositions.add(buffer.readMedium());
            blockEntities.add(buffer.readVarInt());
        }
    }

    public S2CChunkDataPacket(ChunkVec pos, ChunkBuildInfo info, Storage<BlockState> storage, Storage<Biome> biomeStorage, Collection<BlockEntity> blockEntities) {
        this.pos = pos;
        this.info = info;
        this.storage = storage;
        this.biomeStorage = biomeStorage;

        for (BlockEntity blockEntity : blockEntities) {
            BlockVec bPos = blockEntity.pos().chunkLocal();
            this.blockEntityPositions.add((bPos.getIntX() % 16) << 20 | (bPos.getIntY() % 65536) << 4 | bPos.getIntZ() % 16);
            this.blockEntities.add(blockEntity.getType().getRawId());
        }
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.pos);
        this.info.toBytes(buffer);
        this.storage.write(buffer, (encode, block) -> block.write(encode));
        this.biomeStorage.write(buffer, (encode, biome) -> {
            if (biome == null) {
                encode.writeShort(Registries.BIOME.getRawId(Biomes.VOID));
                return;
            }
            encode.writeShort(Registries.BIOME.getRawId(biome));
        });

        buffer.writeVarInt(this.blockEntities.size());
        for (int blockEntity : this.blockEntities) {
            buffer.writeMedium(blockEntityPositions.getInt(blockEntity));
            buffer.writeVarInt(blockEntity);
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        Map<BlockVec, BlockEntityType<?>> blockEntities = new HashMap<>();
        int i = 0;
        for (Integer blkEntityVec : this.blockEntityPositions) {
            int x = (blkEntityVec >> 16) & 0xFF;
            int y = (blkEntityVec >> 8) & 0xFF;
            int z = blkEntityVec & 0xFF;
            blockEntities.put(new BlockVec(x, y, z, BlockVecSpace.WORLD).chunkLocal(), Registries.BLOCK_ENTITY_TYPE.byId(this.blockEntities.getInt(i)));
        }

        handler.onChunkData(this.pos, this.info, this.storage, this.biomeStorage, blockEntities);
    }

    public ChunkVec pos() {
        return this.pos;
    }

    public Storage<BlockState> storage() {
        return this.storage;
    }

    public Storage<Biome> biomeStorage() {
        return this.biomeStorage;
    }

    public IntList blockEntityPositions() {
        return this.blockEntityPositions;
    }

    public IntList blockEntities() {
        return this.blockEntities;
    }
}
