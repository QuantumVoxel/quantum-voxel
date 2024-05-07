package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.collection.PaletteStorage;
import dev.ultreon.quantum.collection.Storage;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.ChunkPos;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.biome.Biomes;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class S2CChunkDataPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;
    private final Storage<BlockProperties> storage;
    private final Storage<Biome> biomeStorage;
    private final IntList blockEntityPositions = new IntArrayList();
    private final IntList blockEntities = new IntArrayList();
    public static final int MAX_SIZE = 1048576;

    public S2CChunkDataPacket(PacketIO buffer) {
        this.pos = buffer.readChunkPos();
        this.storage = new PaletteStorage<>(BlockProperties.AIR, buffer, PacketIO::readBlockMeta);
        this.biomeStorage = new PaletteStorage<>(Biomes.PLAINS, buffer, buf -> Registries.BIOME.byId(buf.readShort()));

        int blockEntityCount = buffer.readVarInt();
        for (int i = 0; i < blockEntityCount; i++) {
            blockEntityPositions.add(buffer.readMedium());
            blockEntities.add(buffer.readVarInt());
        }
    }

    public S2CChunkDataPacket(ChunkPos pos, Storage<BlockProperties> storage, Storage<Biome> biomeStorage, Collection<BlockEntity> blockEntities) {
        this.pos = pos;
        this.storage = storage;
        this.biomeStorage = biomeStorage;

        for (BlockEntity blockEntity : blockEntities) {
            BlockPos bPos = World.toLocalBlockPos(blockEntity.pos());
            this.blockEntityPositions.add((bPos.x() % 16) << 20 | (bPos.y() % 65536) << 4 | bPos.z() % 16);
            this.blockEntities.add(blockEntity.getType().getRawId());
        }
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeChunkPos(this.pos);
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
        Map<BlockPos, BlockEntityType<?>> blockEntities = new HashMap<>();
        int i = 0;
        for (Integer blockEntityPosition : this.blockEntityPositions) {
            int x = (blockEntityPosition >> 20) & 0xF;
            int y = (blockEntityPosition >> 4) & 0xFFFF;
            int z = blockEntityPosition & 0xF;
            blockEntities.put(World.toLocalBlockPos(x, y, z), Registries.BLOCK_ENTITY_TYPE.byId(this.blockEntities.getInt(i)));
        }

        handler.onChunkData(this.pos, this.storage, this.biomeStorage, blockEntities);
    }
}
