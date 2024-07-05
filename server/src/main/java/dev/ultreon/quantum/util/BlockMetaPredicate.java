package dev.ultreon.quantum.util;

import dev.ultreon.quantum.block.state.BlockProperties;

import java.util.function.Predicate;

public class BlockMetaPredicate implements Predicate<BlockProperties> {
    public static final BlockMetaPredicate TRANSPARENT = new BlockMetaPredicate(BlockProperties::isTransparent);
    public static final BlockMetaPredicate FLUID = new BlockMetaPredicate(BlockProperties::isFluid);
    public static final BlockMetaPredicate SOLID = new BlockMetaPredicate(BlockProperties::hasCollider);
    public static final BlockMetaPredicate NON_FLUID = new BlockMetaPredicate(block -> !block.isFluid());
    public static final BlockMetaPredicate REPLACEABLE = new BlockMetaPredicate(BlockProperties::isReplaceable);
    public static final BlockMetaPredicate BREAK_INSTANTLY = new BlockMetaPredicate(block -> block.getHardness() == 0);
    public static final BlockMetaPredicate WG_HEIGHT_CHK = new BlockMetaPredicate(block -> block.isAir() || block.isFluid());
    public static final BlockMetaPredicate EVERYTHING = new BlockMetaPredicate(block -> true);

    private final Predicate<BlockProperties> predicate;

    BlockMetaPredicate(Predicate<BlockProperties> predicate) {

        this.predicate = predicate;
    }

    @Override
    public boolean test(BlockProperties block) {
        return this.predicate.test(block);
    }
}
