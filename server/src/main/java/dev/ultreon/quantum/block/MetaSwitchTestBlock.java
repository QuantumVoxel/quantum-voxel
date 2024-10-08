package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class MetaSwitchTestBlock extends Block {
    public MetaSwitchTestBlock() {

    }

    @Override
    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        BlockState metadata = world.get(pos);
        BlockDataEntry<Boolean> test = metadata.getProperty("on");
        metadata = metadata.withEntry("on", test.map(b -> !b));

        world.set(pos, metadata);

        return UseResult.ALLOW;
    }

    @Override
    public @NotNull BlockState getDefaultState() {
        return super.getDefaultState().withEntry("on", BlockDataEntry.of(false));
    }
}
