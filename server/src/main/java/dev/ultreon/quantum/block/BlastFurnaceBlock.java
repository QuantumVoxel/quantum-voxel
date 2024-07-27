package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.CubicDirection;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;

public class BlastFurnaceBlock extends Block {
    public BlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockProperties createMeta() {
        return super.createMeta().withEntry("lit", BlockDataEntry.of(false)).withEntry("facing", BlockDataEntry.ofEnum(CubicDirection.NORTH));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockProperties onPlacedBy(WorldAccess world, BlockPos blockPos, BlockProperties blockMeta, Player player, ItemStack stack, CubicDirection direction) {
        System.out.println("On placed by " + player.getName() + " at " + blockPos + " facing " + direction);

        return blockMeta.withEntry("facing", BlockDataEntry.ofEnum(direction));
    }

    @Override
    public void onPlace(World world, BlockPos pos, BlockProperties blockProperties) {
        super.onPlace(world, pos, blockProperties);
    }
}
