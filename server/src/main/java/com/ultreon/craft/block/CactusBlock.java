package com.ultreon.craft.block;

import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public class CactusBlock extends Block {
    public CactusBlock() {
    }

    public CactusBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BoundingBox getBoundingBox(int x, int y, int z, BlockMetadata blockMetadata) {
        return new BoundingBox(new Vec3d(x + 1 / 16.0, y, z + 1 / 16.0), new Vec3d(x + 1 - 1 / 16.0, y + 1, z + 1 - 1 / 16.0));
    }
}
