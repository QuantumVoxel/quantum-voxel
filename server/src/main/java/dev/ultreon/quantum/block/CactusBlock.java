package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.BlockFlags;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class CactusBlock extends Block {
    public CactusBlock() {
    }

    public CactusBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BoundingBox getBoundingBox(int x, int y, int z, BlockState blockState) {
        return new BoundingBox(new Vec3d(x + 1 / 16.0, y, z + 1 / 16.0), new Vec3d(x + 1 - 1 / 16.0, y + 1, z + 1 - 1 / 16.0));
    }

    @Override
    public void randomTick(@NotNull ServerWorld world, BlockVec position, BlockState blockState) {
        super.randomTick(world, position, blockState);

        if (ThreadLocalRandom.current().nextInt(4) != 0) return;

        int c = 0;
        for (int dy = 0; dy > -3; dy--) {
            if (world.get(position.offset(0, dy, 0)).is(this))
                c++;
        }

        if (c == 3) return;

        BlockVec offset = position.offset(0, 1, 0);
        if (world.get(offset).isAir())
            world.set(offset, Blocks.CACTUS.getDefaultState(), BlockFlags.UPDATE | BlockFlags.SYNC);
    }
}
