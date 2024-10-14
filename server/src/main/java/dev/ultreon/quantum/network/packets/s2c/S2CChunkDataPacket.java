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
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record S2CChunkDataPacket(ChunkVec pos, ChunkBuildInfo info, Storage<BlockState> storage, @NotNull Storage<RegistryKey<Biome>> biomeStorage, IntList blockEntityPositions, IntList blockEntities) implements Packet<InGameClientPacketHandler> {
    public static final int MAX_SIZE = 1048576;

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
        var biomeStorage = new PaletteStorage<>(RegistryKey.of(RegistryKeys.BIOME, new NamespaceID("unknown")), buffer, buf -> RegistryKey.of(RegistryKeys.BIOME, buf.readId()));

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
                encode.writeId(new NamespaceID("error"));
                return;
            }
            encode.writeId(biome.id());
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
}
