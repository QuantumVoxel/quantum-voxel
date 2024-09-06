package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public abstract class BlockChangeEvent extends BlockEvent {
    private final @NotNull BlockState originalState;
    private final @NotNull BlockState newState;

    public BlockChangeEvent(@NotNull WorldAccess world,
                            @NotNull BlockState originalState,
                            @NotNull BlockState newState,
                            @NotNull BlockVec position) {
        super(world, newState, position);
        this.originalState = originalState;
        this.newState = newState;
    }

    public @NotNull BlockState getOriginalState() {
        return this.originalState;
    }

    public @NotNull Block getOriginalBlock() {
        return this.originalState.getBlock();
    }

    public @NotNull BlockState getNewState() {
        return this.newState;
    }

    public @NotNull Block getNewBlock() {
        return this.newState.getBlock();
    }
}
