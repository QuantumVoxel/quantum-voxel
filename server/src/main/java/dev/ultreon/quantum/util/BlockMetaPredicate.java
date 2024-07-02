package dev.ultreon.quantum.util;

import dev.ultreon.quantum.block.state.BlockData;

import java.util.function.Predicate;

public class BlockMetaPredicate implements Predicate<BlockData> {
    public static final BlockMetaPredicate TRANSPARENT = new BlockMetaPredicate(BlockData::isTransparent);
    public static final BlockMetaPredicate FLUID = new BlockMetaPredicate(BlockData::isFluid);
    public static final BlockMetaPredicate SOLID = new BlockMetaPredicate(BlockData::hasCollider);
    public static final BlockMetaPredicate NON_FLUID = new BlockMetaPredicate(block -> !block.isFluid());
    public static final BlockMetaPredicate REPLACEABLE = new BlockMetaPredicate(BlockData::isReplaceable);
    public static final BlockMetaPredicate BREAK_INSTANTLY = new BlockMetaPredicate(block -> block.getHardness() == 0);
    public static final BlockMetaPredicate WG_HEIGHT_CHK = new BlockMetaPredicate(block -> block.isAir() || block.isFluid());
    public static final BlockMetaPredicate EVERYTHING = new BlockMetaPredicate(block -> true);

    private final Predicate<BlockData> predicate;

    BlockMetaPredicate(Predicate<BlockData> predicate) {

        this.predicate = predicate;
    }

    @Override
    public boolean test(BlockData block) {
        return this.predicate.test(block);
    }
}
