package dev.ultreon.quantum.block;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.block.state.BlockData;
import dev.ultreon.quantum.util.BoundingBox;

public class CactusBlock extends Block {
    public CactusBlock() {
    }

    public CactusBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BoundingBox getBoundingBox(int x, int y, int z, BlockData blockData) {
        return new BoundingBox(new Vec3d(x + 1 / 16.0, y, z + 1 / 16.0), new Vec3d(x + 1 - 1 / 16.0, y + 1, z + 1 - 1 / 16.0));
    }
}
