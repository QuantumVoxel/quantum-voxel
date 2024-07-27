package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

public class MetaSwitchTestBlock extends Block {
    public MetaSwitchTestBlock() {

    }

    @Override
    public UseResult use(WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockPos pos) {
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
