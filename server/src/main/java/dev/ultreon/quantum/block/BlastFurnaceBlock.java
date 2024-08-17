package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.CubicDirection;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class BlastFurnaceBlock extends Block {
    public BlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull BlockState createMeta() {
        return super.createMeta().withEntry("lit", BlockDataEntry.of(false)).withEntry("facing", BlockDataEntry.ofEnum(CubicDirection.NORTH));
    }

    @Override
    public void onPlace(@NotNull World world, @NotNull BlockVec pos, @NotNull BlockState blockState) {
        super.onPlace(world, pos, blockState);
    }
}
