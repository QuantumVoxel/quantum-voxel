package dev.ultreon.quantum.util;

import dev.ultreon.quantum.block.BlockState;

import java.util.function.Predicate;

public class BlockMetaPredicate implements Predicate<BlockState> {
    public static final BlockMetaPredicate TRANSPARENT = new BlockMetaPredicate(BlockState::isTransparent);
    public static final BlockMetaPredicate FLUID = new BlockMetaPredicate(BlockState::isFluid);
    public static final BlockMetaPredicate SOLID = new BlockMetaPredicate(BlockState::hasCollider);
    public static final BlockMetaPredicate NON_FLUID = new BlockMetaPredicate(block -> !block.isFluid());
    public static final BlockMetaPredicate REPLACEABLE = new BlockMetaPredicate(BlockState::isReplaceable);
    public static final BlockMetaPredicate BREAK_INSTANTLY = new BlockMetaPredicate(block -> block.getHardness() == 0);
    public static final BlockMetaPredicate WG_HEIGHT_CHK = new BlockMetaPredicate(block -> block.isAir() || block.isFluid());
    public static final BlockMetaPredicate EVERYTHING = new BlockMetaPredicate(block -> true);

    private final Predicate<BlockState> predicate;

    BlockMetaPredicate(Predicate<BlockState> predicate) {

        this.predicate = predicate;
    }

    @Override
    public boolean test(BlockState block) {
        return this.predicate.test(block);
    }
}
