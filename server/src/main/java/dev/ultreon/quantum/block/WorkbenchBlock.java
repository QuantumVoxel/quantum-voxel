package dev.ultreon.quantum.block;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.NotNull;

public class WorkbenchBlock extends Block {
    public WorkbenchBlock(Block.@This Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        if (world.isServerSide()) player.openAdvancedCrafting(pos);
        return UseResult.SKIP;
    }
}
