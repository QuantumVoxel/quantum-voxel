package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.block.SlabBlock;
import dev.ultreon.quantum.world.Direction;

public class StateProperties {
    public static final StatePropertyKey<Boolean> ABSORB_LIGHT = new BoolPropertyKey("absorb_light");
    public static final StatePropertyKey<SlabBlock.Type> SLAB_TYPE = new EnumPropertyKey<>("type", SlabBlock.Type.class);
    public static final StatePropertyKey<Boolean> LIT = new BoolPropertyKey("lit");
    public static final StatePropertyKey<Direction> FACING = new EnumPropertyKey<>("facing", Direction.class, Direction.HORIZONTAL);
}
