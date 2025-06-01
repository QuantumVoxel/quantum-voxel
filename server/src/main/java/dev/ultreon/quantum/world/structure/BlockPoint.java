package dev.ultreon.quantum.world.structure;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;

import java.util.Objects;
import java.util.function.Supplier;

public final class BlockPoint {
    private final BlockVec pos;
    private final Supplier<BlockState> stateGetter;

    public BlockPoint(BlockVec pos, Supplier<BlockState> stateGetter) {
        if (pos.getSpace() != BlockVecSpace.WORLD) {
            throw new IllegalArgumentException("BlockPoint must be in the world space");
        }
        this.pos = pos;
        this.stateGetter = stateGetter;
    }

    public BlockState state() {
        return stateGetter.get();
    }

    public BlockVec pos() {
        return pos;
    }

    public Supplier<BlockState> stateGetter() {
        return stateGetter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlockPoint) obj;
        return Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.stateGetter, that.stateGetter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, stateGetter);
    }

    @Override
    public String toString() {
        return "BlockPoint[" +
               "pos=" + pos + ", " +
               "stateGetter=" + stateGetter + ']';
    }

}
