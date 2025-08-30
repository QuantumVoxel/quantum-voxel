package dev.ultreon.quantum.network.packets.s2c;

import com.badlogic.gdx.utils.IntArray;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.BlockState;
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
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class S2CChunkDataPacket implements Packet<InGameClientPacketHandler> {
    public static final int MAX_SIZE = 1048576;
    private final ChunkVec pos;
    private final ChunkBuildInfo info;
    private final Storage<@NotNull BlockState> storage;
    private final @NotNull Storage<@NotNull RegistryKey<Biome>> biomeStorage;
    private final IntArray blockEntityPositions;
    private final IntArray blockEntities;
    private final Map<BlockVec, BlockEntityType<?>> blockEntitiesByLocation = new HashMap<>();

    public S2CChunkDataPacket(ChunkVec pos, ChunkBuildInfo info, Storage<@NotNull BlockState> storage, @NotNull Storage<@NotNull RegistryKey<Biome>> biomeStorage, IntArray blockEntityPositions, IntArray blockEntities) {
        this.pos = pos;
        this.info = info;
        this.storage = storage;
        this.biomeStorage = biomeStorage;
        this.blockEntityPositions = blockEntityPositions;
        this.blockEntities = blockEntities;
    }

    public S2CChunkDataPacket(ChunkVec pos, ChunkBuildInfo info, Storage<@NotNull BlockState> storage, @NotNull Storage<@NotNull RegistryKey<Biome>> biomeStorage, Collection<BlockEntity> blockEntities) {
        this(pos, info, storage, biomeStorage, new IntArray(), new IntArray());

        for (BlockEntity blockEntity : blockEntities) {
            BlockVec bPos = blockEntity.pos().chunkLocal();
            this.blockEntityPositions.add((bPos.getIntX() % 16) << 20 | (bPos.getIntY() % 65536) << 4 | bPos.getIntZ() % 16);
            this.blockEntities.add(blockEntity.getType().getRawId());
        }
    }

    public static S2CChunkDataPacket read(PacketIO buffer) {
        ChunkVec pos = buffer.readChunkVec();
        ChunkBuildInfo info = new ChunkBuildInfo(buffer);
        PaletteStorage<@NotNull BlockState> storage = new PaletteStorage<>(Blocks.AIR.getDefaultState(), buffer, PacketIO::readBlockState);
        PaletteStorage<@NotNull RegistryKey<Biome>> biomeStorage = new PaletteStorage<>(RegistryKey.of(RegistryKeys.BIOME, new NamespaceID("unknown")), buffer, buf -> Registries.BIOME.nameById(buf.readVarInt()));

        IntArray blockEntityPositions = new IntArray();
        IntArray blockEntities = new IntArray();

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

        buffer.writeVarInt(this.blockEntities.size);
        IntArray blockEntitiesBck = this.blockEntities;
        for (int beIdx = 0, entitiesSize = blockEntitiesBck.size; beIdx < entitiesSize; beIdx++) {
            int blockEntity = blockEntitiesBck.get(beIdx);
            buffer.writeMedium(blockEntityPositions.get(beIdx));
            buffer.writeVarInt(blockEntity);
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        blockEntitiesByLocation.clear();
        int i = 0;
        for (int blkEntityVec : this.blockEntityPositions.toArray()) {
            int x = (blkEntityVec >> 16) & 0xFF;
            int y = (blkEntityVec >> 8) & 0xFF;
            int z = blkEntityVec & 0xFF;
            blockEntitiesByLocation.put(new BlockVec(x, y, z).chunkLocal(), Registries.BLOCK_ENTITY_TYPE.byRawId(this.blockEntities.get(i)));
        }

        handler.onChunkData(this.pos, this.info, this.storage, this.biomeStorage, blockEntitiesByLocation);
    }

    public ChunkVec pos() {
        return pos;
    }

    public ChunkBuildInfo info() {
        return info;
    }

    public Storage<@NotNull BlockState> storage() {
        return storage;
    }

    public @NotNull Storage<@NotNull RegistryKey<Biome>> biomeStorage() {
        return biomeStorage;
    }

    public IntArray blockEntityPositions() {
        return blockEntityPositions;
    }

    public IntArray blockEntities() {
        return blockEntities;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        S2CChunkDataPacket that = (S2CChunkDataPacket) obj;
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
