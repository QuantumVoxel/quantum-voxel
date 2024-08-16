package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.CubicDirection;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public class BlastFurnaceBlock extends Block {
    public BlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull BlockProperties createMeta() {
        return super.createMeta().withEntry("lit", BlockDataEntry.of(false)).withEntry("facing", BlockDataEntry.ofEnum(CubicDirection.NORTH));
    }

    @Override
    public void onPlace(@NotNull World world, @NotNull BlockVec pos, @NotNull BlockProperties blockProperties) {
        super.onPlace(world, pos, blockProperties);
    }
}
