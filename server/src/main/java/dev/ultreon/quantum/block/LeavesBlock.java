package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class LeavesBlock extends Block {
    public LeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(@NotNull ServerWorld world, BlockVec position, BlockState blockState) {
        super.randomTick(world, position, blockState);

        // 1/2 chance of breaking each random tick
//        if (ThreadLocalRandom.current().nextInt(2) == 0) {
//            // Only break if there is a log nearby.
//            for (int dx = -2; dx <= 2; dx++) {
//                for (int dy = -2; dy <= 2; dy++) {
//                    for (int dz = -2; dz <= 2; dz++) {
//                        if (world.get(position.offset(dx, dy, dz)).is(Blocks.LOG)) {
//                            return;
//                        }
//                    }
//                }
//            }
//            world.set(position, Blocks.AIR.getDefaultState(), BlockFlags.UPDATE | BlockFlags.SYNC | BlockFlags.DESTROY | BlockFlags.LIGHT);
//        }
    }
}
