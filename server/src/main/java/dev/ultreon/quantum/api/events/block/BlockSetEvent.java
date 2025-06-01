package dev.ultreon.quantum.api.events.block;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class BlockSetEvent extends BlockEvent {
    private final @NotNull BlockState original;

    public BlockSetEvent(World world, BlockVec position, @NotNull BlockState state, int flags) {
        super(world, state, position);
        this.original = world.get(position);
    }

    public @NotNull BlockState getOriginalState() {
        return this.original;
    }

    public @NotNull Block getOriginalBlock() {
        return this.original.getBlock();
    }
}
