package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.api.events.world.WorldAccessEvent;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public abstract class BlockEvent extends WorldAccessEvent {
    private final @NotNull BlockState state;
    private final @NotNull BlockVec position;

    public BlockEvent(@NotNull WorldAccess world,
                      @NotNull BlockState state,
                      @NotNull BlockVec position) {
        super(world);
        this.state = state;
        this.position = position;
    }

    public @NotNull BlockState getState() {
        return this.state;
    }

    public @NotNull Block getBlock() {
        return this.state.getBlock();
    }

    public @NotNull BlockVec getPosition() {
        return this.position;
    }
}
