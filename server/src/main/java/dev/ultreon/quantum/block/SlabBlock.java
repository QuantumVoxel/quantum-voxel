package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockData;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.world.BlockPos;

public class SlabBlock extends Block {
    public SlabBlock() {
        super();
    }

    public SlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BoundingBox getBoundingBox(int x, int y, int z, BlockData blockData) {
        var type = blockData.<Type>get("type");

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
    public BlockData createMeta() {
        return super.createMeta().withEntry("type", BlockDataEntry.ofEnum(Type.BOTTOM));
    }

    @Override
    public BlockData onPlacedBy(BlockData blockMeta, BlockPos pos, UseItemContext context) {
        double y = context.result().getPosition().y % 1;
        return blockMeta.withEntry("type", y < 0.5 ? Type.TOP : Type.BOTTOM);
    }

    public enum Type {
        BOTTOM,
        TOP,
        DOUBLE
    }
}
