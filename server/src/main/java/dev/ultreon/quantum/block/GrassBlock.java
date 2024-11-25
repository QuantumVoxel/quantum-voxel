package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.BlockFlags;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.block.Blocks.DIRT;

public class GrassBlock extends Block {
    public GrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(@NotNull ServerWorld world, BlockVec position, BlockState blockState) {
        super.randomTick(world, position, blockState);

        BlockState above = world.get(position.offset(0, 1, 0));
        if (!above.isAir() && !above.isTransparent()) {
            world.set(position.offset(0, 0, 0), DIRT.getDefaultState(), BlockFlags.UPDATE | BlockFlags.SYNC);
            return;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (world.get(position.offset(dx, dy, dz)).is(DIRT) && world.get(position.offset(dx, dy + 1, dz)).isAir()) {
                        world.set(position.offset(dx, dy, dz), getDefaultState(), BlockFlags.UPDATE | BlockFlags.SYNC);

                        world.sendMessage("[green]Grass grew in " + position.offset(dx, dy, dz) + "!");
                    }
                }
            }
        }
    }
}
