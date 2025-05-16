package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.block.Blocks;
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
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.ChunkBuildInfo;
import dev.ultreon.quantum.world.gen.biome.Biomes;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class S2CChunkDataPacket implements Packet<InGameClientPacketHandler> {
    public static final int MAX_SIZE = 1048576;
    private final ChunkVec pos;
    private final ChunkBuildInfo info;
    private final Storage<BlockState> storage;
    private final @NotNull Storage<RegistryKey<Biome>> biomeStorage;
    private final IntList blockEntityPositions;
    private final IntList blockEntities;
    private static final ThreadLocal<Map<BlockVec, BlockEntityType<?>>> blockEntitiesByLocation = ThreadLocal.withInitial(HashMap::new);

    public S2CChunkDataPacket(ChunkVec pos, ChunkBuildInfo info, Storage<BlockState> storage, @NotNull Storage<RegistryKey<Biome>> biomeStorage, IntList blockEntityPositions, IntList blockEntities) {
        this.pos = pos;
        this.info = info;
        this.storage = storage;
        this.biomeStorage = biomeStorage;
        this.blockEntityPositions = blockEntityPositions;
        this.blockEntities = blockEntities;
    }

    public S2CChunkDataPacket(ChunkVec pos, ChunkBuildInfo info, Storage<BlockState> storage, @NotNull Storage<RegistryKey<Biome>> biomeStorage, Collection<BlockEntity> blockEntities) {
        this(pos, info, storage, biomeStorage, new IntArrayList(), new IntArrayList());

        for (BlockEntity blockEntity : blockEntities) {
            BlockVec bPos = blockEntity.pos().chunkLocal();
            this.blockEntityPositions.add((bPos.getIntX() % 16) << 20 | (bPos.getIntY() % 65536) << 4 | bPos.getIntZ() % 16);
            this.blockEntities.add(blockEntity.getType().getRawId());
        }
    }

    public static S2CChunkDataPacket read(PacketIO buffer) {
        var pos = buffer.readChunkVec();
        var info = new ChunkBuildInfo(buffer);
        var storage = new PaletteStorage<>(Blocks.AIR.getDefaultState(), buffer, PacketIO::readBlockState);
        var biomeStorage = new PaletteStorage<>(RegistryKey.of(RegistryKeys.BIOME, new NamespaceID("unknown")), buffer, buf -> Registries.BIOME.nameById(buf.readVarInt()));

        var blockEntityPositions = new IntArrayList();
        var blockEntities = new IntArrayList();

        int blockEntityCount = buffer.readVarInt();
        for (int i = 0; i < blockEntityCount; i++) {
            blockEntityPositions.add(buffer.readMedium());
            blockEntities.add(buffer.readVarInt());
        }

        return new S2CChunkDataPacket(pos, info, storage, biomeStorage, blockEntityPositions, blockEntities);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkVec(this.pos);
        this.info.toBytes(buffer);
        this.storage.write(buffer, (encode, block) -> block.write(encode));
        this.biomeStorage.write(buffer, (encode, biome) -> {
            if (biome == null) {
                encode.writeVarInt(encode.get(RegistryKeys.BIOME).idByName(Biomes.VOID));
                return;
            }
            encode.writeVarInt(encode.get(biome.parent()).idByName(biome));
        });

        buffer.writeVarInt(this.blockEntities.size());
        IntList blockEntitiesBck = this.blockEntities;
        for (int beIdx = 0, entitiesSize = blockEntitiesBck.size(); beIdx < entitiesSize; beIdx++) {
            int blockEntity = blockEntitiesBck.getInt(beIdx);
            buffer.writeMedium(blockEntityPositions.getInt(beIdx));
            buffer.writeVarInt(blockEntity);
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        blockEntitiesByLocation.get().clear();
        int i = 0;
        for (Integer blkEntityVec : this.blockEntityPositions) {
            int x = (blkEntityVec >> 16) & 0xFF;
            int y = (blkEntityVec >> 8) & 0xFF;
            int z = blkEntityVec & 0xFF;
            blockEntitiesByLocation.get().put(new BlockVec(x, y, z, BlockVecSpace.WORLD).chunkLocal(), Registries.BLOCK_ENTITY_TYPE.byRawId(this.blockEntities.getInt(i)));
        }

        handler.onChunkData(this.pos, this.info, this.storage, this.biomeStorage, blockEntitiesByLocation.get());
    }

    public ChunkVec pos() {
        return pos;
    }

    public ChunkBuildInfo info() {
        return info;
    }

    public Storage<BlockState> storage() {
        return storage;
    }

    public @NotNull Storage<RegistryKey<Biome>> biomeStorage() {
        return biomeStorage;
    }

    public IntList blockEntityPositions() {
        return blockEntityPositions;
    }

    public IntList blockEntities() {
        return blockEntities;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CChunkDataPacket) obj;
        return Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.info, that.info) &&
               Objects.equals(this.storage, that.storage) &&
               Objects.equals(this.biomeStorage, that.biomeStorage) &&
               Objects.equals(this.blockEntityPositions, that.blockEntityPositions) &&
               Objects.equals(this.blockEntities, that.blockEntities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, info, storage, biomeStorage, blockEntityPositions, blockEntities);
    }

    @Override
    public String toString() {
        return "S2CChunkDataPacket[" +
               "pos=" + pos + ", " +
               "info=" + info + ", " +
               "storage=" + storage + ", " +
               "biomeStorage=" + biomeStorage + ", " +
               "blockEntityPositions=" + blockEntityPositions + ", " +
               "blockEntities=" + blockEntities + ']';
    }

}
