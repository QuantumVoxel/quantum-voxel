package com.ultreon.quantum.block;

import com.ultreon.quantum.block.state.BlockDataEntry;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.UseResult;
import com.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public class MetaSwitchTestBlock extends Block {
    public MetaSwitchTestBlock() {

    }

    @Override
    public UseResult use(@NotNull World world, @NotNull Player player, @NotNull Item item, @NotNull BlockPos pos) {
        BlockProperties metadata = world.get(pos);
        BlockDataEntry<Boolean> test = metadata.getProperty("on");
        metadata = metadata.withEntry("on", test.map(b -> !b));

        world.set(pos, metadata);

        return UseResult.ALLOW;
    }

    @Override
    public BlockProperties createMeta() {
        return super.createMeta().withEntry("on", BlockDataEntry.of(false));
    }
}
