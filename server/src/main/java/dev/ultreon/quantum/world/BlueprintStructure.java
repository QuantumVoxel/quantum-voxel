package dev.ultreon.quantum.world;

import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.world.structure.BlockPoint;
import dev.ultreon.quantum.world.structure.Structure;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;

public class BlueprintStructure extends Structure {
    private final Blueprint blueprint;
    private BoundingBox boundingBox;

    public BlueprintStructure(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    @Override
    public void place(BlockSetter world, int x, int y, int z) {
        for (BlockPoint point : blueprint) {
            BlockVec pos = point.pos().cpy().add(x, y, z);
            world.set(pos, point.state());
        }
    }

    @Override
    public BlockVec getSize() {
        if (boundingBox == null)
            boundingBox = blueprint.calcBoundingBox();

        return new BlockVec(boundingBox.getWidth(), boundingBox.getHeight(), boundingBox.getDepth(), BlockVecSpace.WORLD);
    }

    @Override
    public BlockVec getCenter() {
        if (boundingBox == null)
            boundingBox = blueprint.calcBoundingBox();

        return new BlockVec(boundingBox.getCenterX(), boundingBox.getCenterY(), boundingBox.getCenterZ(), BlockVecSpace.WORLD);
    }
}
