package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityTypes;
import dev.ultreon.quantum.block.entity.CrateBlockEntity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.World;
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
