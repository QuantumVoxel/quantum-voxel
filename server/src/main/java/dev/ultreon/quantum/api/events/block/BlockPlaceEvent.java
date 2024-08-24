package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class BlockPlaceEvent extends BlockChangeEvent {
    public BlockPlaceEvent(@NotNull WorldAccess world,
                           @NotNull BlockState originalState,
                           @NotNull BlockState newState,
                           @NotNull BlockVec position) {
        super(world, originalState, newState, position);
    }
}
