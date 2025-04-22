package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.*;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.world.vec.BlockVec;

public class SlabBlock extends Block {
    public SlabBlock() {
        super();
    }

    public SlabBlock(Properties properties) {
        super(properties);

        definition.setDefault(definition.empty().with(StateProperties.SLAB_TYPE, Type.BOTTOM));
    }

    @Override
    public BoundingBox getBoundingBox(int x, int y, int z, BlockState blockState) {
        var type = blockState.<Type>get("type");

        switch (type) {
            case TOP:
                return new BoundingBox(x, y, z, x + 1, y + 1, z + 1);
            case BOTTOM:
                return new BoundingBox(x, y + 0.5, z, x + 1, y, z + 1);
            case DOUBLE:
                return new BoundingBox(x, y, z, x + 1, y + 0.5, z + 1);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected void defineState(BlockStateDefinition.Builder definition) {
        super.defineState(definition);

        definition.add(StateProperties.SLAB_TYPE);
    }

    @Override
    public BlockState onPlacedBy(BlockState blockMeta, BlockVec pos, UseItemContext context) {
        double y = context.result().getVec().y % 1;
        return blockMeta.with(StateProperties.SLAB_TYPE, y < 0.5 ? Type.TOP : Type.BOTTOM);
    }

    public enum Type implements StringSerializable {
        BOTTOM,
        TOP,
        DOUBLE;

        @Override
        public String serialize() {
            return name().toLowerCase();
        }
    }
}
