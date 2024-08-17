package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockAttemptBreakEvent extends BlockChangeEvent implements Cancelable {
    private final @Nullable ItemStack usedItem;
    private final @Nullable ServerPlayer breaker;
    private boolean canceled;

    public BlockAttemptBreakEvent(@NotNull ServerWorld world,
                                  @NotNull BlockVec pos,
                                  @NotNull BlockState originalState,
                                  @NotNull BlockState newState,
                                  @Nullable ItemStack usedItem,
                                  @Nullable ServerPlayer breaker) {
        super(world, originalState, newState, pos);
        this.usedItem = usedItem;
        this.breaker = breaker;
    }

    public @Nullable ItemStack getUsedItem() {
        return this.usedItem;
    }

    public @Nullable ServerPlayer getBreaker() {
        return this.breaker;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
