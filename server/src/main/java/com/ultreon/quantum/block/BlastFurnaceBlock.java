package com.ultreon.quantum.block;

import com.ultreon.quantum.block.state.BlockDataEntry;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.CubicDirection;
import com.ultreon.quantum.world.World;

public class BlastFurnaceBlock extends Block {
    public BlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockProperties createMeta() {
        return super.createMeta().withEntry("lit", BlockDataEntry.of(false)).withEntry("facing", BlockDataEntry.ofEnum(CubicDirection.NORTH));
    }

    @Override
    public BlockProperties onPlacedBy(World world, BlockPos blockPos, BlockProperties blockMeta, Player player, ItemStack stack, CubicDirection direction) {
        System.out.println("On placed by " + player.getName() + " at " + blockPos + " facing " + direction);

        return blockMeta.withEntry("facing", BlockDataEntry.ofEnum(direction));
    }

    @Override
    public void onPlace(World world, BlockPos pos, BlockProperties blockProperties) {
        super.onPlace(world, pos, blockProperties);
    }
}
