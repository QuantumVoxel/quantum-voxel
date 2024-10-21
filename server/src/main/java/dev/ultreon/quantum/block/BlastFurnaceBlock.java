package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.block.state.BlockStateDefinition;
import dev.ultreon.quantum.block.state.StateProperties;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class BlastFurnaceBlock extends Block {
    public BlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void defineState(BlockStateDefinition definition) {
        super.defineState(definition);

        definition.set(StateProperties.LIT, false);
        definition.set(StateProperties.FACING, Direction.NORTH);
    }

    @Override
    public void onPlace(@NotNull World world, @NotNull BlockVec pos, @NotNull BlockState blockState) {
        super.onPlace(world, pos, blockState);
    }
}
