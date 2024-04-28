package com.ultreon.quantum.block;

import com.ultreon.quantum.block.entity.BlockEntity;
import com.ultreon.quantum.block.entity.BlockEntityTypes;
import com.ultreon.quantum.block.entity.CrateBlockEntity;
import com.ultreon.quantum.entity.player.Player;
import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.UseResult;
import com.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public class CrateBlock extends EntityBlock {
    public CrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull BlockEntity createBlockEntity(World world, BlockPos pos) {
        return BlockEntityTypes.CRATE.create(world, pos);
    }

    @Override
    public UseResult use(@NotNull World world, @NotNull Player player, @NotNull Item item, @NotNull BlockPos pos) {
        super.use(world, player, item, pos);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CrateBlockEntity crate && world.isClientSide()) {
            crate.open(player);
            return UseResult.ALLOW;
        }

        return UseResult.SKIP;
    }
}
