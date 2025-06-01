package dev.ultreon.quantum.world.structure;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.ChunkAccess;

import static dev.ultreon.quantum.world.World.CS;

public class WorldSlice implements BlockSetter {
    private final ChunkAccess chunk;

    public WorldSlice(ChunkAccess chunk) {
        this.chunk = chunk;
        chunk.getWorld();
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        Vec3i offset = chunk.getOffset();
        if (offset.x <= x && offset.y <= y && offset.z <= z && offset.x + CS > x && offset.y + CS > y && offset.z + CS > z) {
            chunk.set(x - offset.x, y - offset.y, z - offset.z, block);
            return true;
        }
        return false;
    }
}
