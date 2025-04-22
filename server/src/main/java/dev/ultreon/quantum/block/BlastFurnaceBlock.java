package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.entity.BlastFurnaceBlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.block.state.BlockStateDefinition;
import dev.ultreon.quantum.block.state.StateProperties;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class BlastFurnaceBlock extends EntityBlock {
    public BlastFurnaceBlock(Properties properties) {
        super(properties);

        definition.setDefault(definition.empty().with(StateProperties.LIT, false).with(StateProperties.FACING, Direction.UP));
    }

    @Override
    protected void defineState(BlockStateDefinition.Builder definition) {
        super.defineState(definition);

        definition.add(StateProperties.LIT, StateProperties.FACING);
    }

    @Override
    public void onPlace(@NotNull World world, @NotNull BlockVec pos, @NotNull BlockState blockState) {
        super.onPlace(world, pos, blockState);
    }

    @Override
    protected @NotNull BlockEntity createBlockEntity(World world, BlockVec pos) {
        return new BlastFurnaceBlockEntity(world, pos);
    }


    @Override
    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        super.use(world, player, item, pos);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BlastFurnaceBlockEntity && world.isClientSide()) {
            BlastFurnaceBlockEntity blastFurnace = (BlastFurnaceBlockEntity) blockEntity;
            blastFurnace.open(player);
            return UseResult.ALLOW;
        }

        return UseResult.SKIP;
    }
}
