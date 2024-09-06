package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockBrokenEvent extends BlockChangeEvent {
    private final @Nullable ItemStack usedItem;
    private final @Nullable Player breaker;

    public BlockBrokenEvent(@NotNull World world,
                            @NotNull BlockVec pos,
                            @NotNull BlockState originalState,
                            @NotNull BlockState newState,
                            @Nullable ItemStack usedItem,
                            @Nullable Player breaker) {
        super(world, originalState, newState, pos);
        this.usedItem = usedItem;
        this.breaker = breaker;
    }

    public @Nullable ItemStack getUsedItem() {
        return this.usedItem;
    }

    public @Nullable Player getBreaker() {
        return this.breaker;
    }
}
