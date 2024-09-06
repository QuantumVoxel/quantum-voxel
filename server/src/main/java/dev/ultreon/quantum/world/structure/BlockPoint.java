package dev.ultreon.quantum.world.structure;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;

import java.util.function.Supplier;

public record BlockPoint(
        BlockVec pos,
        Supplier<BlockState> stateGetter
) {
    public BlockPoint {
        if (pos.getSpace() != BlockVecSpace.WORLD) {
            throw new IllegalArgumentException("BlockPoint must be in the world space");
        }
    }

    public BlockState state() {
        return stateGetter.get();
    }
}
