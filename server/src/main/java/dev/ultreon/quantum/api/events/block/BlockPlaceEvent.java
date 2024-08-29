package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockPlaceEvent extends BlockChangeEvent {
    private final @Nullable Entity placer;

    public BlockPlaceEvent(@NotNull WorldAccess world,
                           @NotNull BlockState originalState,
                           @NotNull BlockState newState,
                           @NotNull BlockVec position, @Nullable Entity placer) {
        super(world, originalState, newState, position);
        this.placer = placer;
    }

    public @Nullable Entity getPlacer() {
        return placer;
    }
}
