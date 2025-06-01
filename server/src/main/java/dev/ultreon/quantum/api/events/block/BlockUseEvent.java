package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockUseEvent extends BlockEvent {
    public BlockUseEvent(@NotNull WorldAccess world,
                         @NotNull BlockState state,
                         @NotNull BlockVec position,
                         @Nullable Player player) {
        super(world, state, position);
    }
}
