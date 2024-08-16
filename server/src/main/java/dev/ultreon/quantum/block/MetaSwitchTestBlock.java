package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

public class MetaSwitchTestBlock extends Block {
    public MetaSwitchTestBlock() {

    }

    @Override
    public @NotNull UseResult use(@NotNull WorldAccess world, @NotNull Player player, @NotNull Item item, @NotNull BlockVec pos) {
        BlockProperties metadata = world.get(pos);
        BlockDataEntry<Boolean> test = metadata.getProperty("on");
        metadata = metadata.withEntry("on", test.map(b -> !b));

        world.set(pos, metadata);

        return UseResult.ALLOW;
    }

    @Override
    public @NotNull BlockProperties createMeta() {
        return super.createMeta().withEntry("on", BlockDataEntry.of(false));
    }
}
