package dev.ultreon.quantum.world;

import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.Point;
import dev.ultreon.quantum.world.structure.BlockPoint;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public final class Blueprint implements Iterable<BlockPoint> {
    private final List<BlockPoint> blocks = new ArrayList<>();

    public void addBlock(BlockPoint block) {
        blocks.add(block);
    }

    public void addBlock(int x, int y, int z, BlockState state) {
        blocks.add(new BlockPoint(new BlockVec(x, y, z), () -> state));
    }

    public void addBlock(int x, int y, int z, Supplier<BlockState> stateSupplier) {
        blocks.add(new BlockPoint(new BlockVec(x, y, z), stateSupplier));
    }

    public void filledBox(int x, int y, int z, int width, int height, int depth, BlockState state) {
        for (int x1 = x; x1 < x + width; x1++) {
            for (int y1 = y; y1 < y + height; y1++) {
                for (int z1 = z; z1 < z + depth; z1++) {
                    addBlock(x1, y1, z1, state);
                }
            }
        }
    }

    public void filledBox(int x, int y, int z, int width, int height, int depth, Supplier<BlockState> stateSupplier) {
        for (int x1 = x; x1 < x + width; x1++) {
            for (int y1 = y; y1 < y + height; y1++) {
                for (int z1 = z; z1 < z + depth; z1++) {
                    addBlock(x1, y1, z1, stateSupplier);
                }
            }
        }
    }

    private void addFrontAndBackBlocks(int x, int y, int z, int width, int height, int depth, Supplier<BlockState> state) {
        for (int x1 = x; x1 < x + width; x1++) {
            for (int y1 = y; y1 < y + height; y1++) {
                addBlock(x1, y1, z, state); // Front
                addBlock(x1, y1, z + depth - 1, state); // Back
            }
        }
    }

    private void addBottomAndTopBlocks(int x, int y, int z, int width, int height, int depth, Supplier<BlockState> state) {
        for (int x1 = x; x1 < x + width; x1++) {
            for (int z1 = z; z1 < z + depth; z1++) {
                addBlock(x1, y, z1, state); // Bottom
                addBlock(x1, y + height - 1, z1, state); // Top
            }
        }
    }

    private void addLeftAndRightBlocks(int x, int y, int z, int width, int height, int depth, Supplier<BlockState> state) {
        for (int y1 = y; y1 < y + height; y1++) {
            for (int z1 = z; z1 < z + depth; z1++) {
                addBlock(x, y1, z1, state); // Left
                addBlock(x + width - 1, y1, z1, state); // Right
            }
        }
    }

    public void box(int x, int y, int z, int width, int height, int depth, BlockState state) {
        Supplier<BlockState> stateSupplier = () -> state;
        addFrontAndBackBlocks(x, y, z, width, height, depth, stateSupplier);
        addBottomAndTopBlocks(x, y, z, width, height, depth, stateSupplier);
        addLeftAndRightBlocks(x, y, z, width, height, depth, stateSupplier);
    }

    public void box(int x, int y, int z, int width, int height, int depth, Supplier<BlockState> stateSupplier) {
        addFrontAndBackBlocks(x, y, z, width, height, depth, stateSupplier);
        addBottomAndTopBlocks(x, y, z, width, height, depth, stateSupplier);
        addLeftAndRightBlocks(x, y, z, width, height, depth, stateSupplier);
    }

    public void addBlocks(List<BlockPoint> blocks) {
        this.blocks.addAll(blocks);
    }

    @Override
    public @NotNull Iterator<BlockPoint> iterator() {
        return blocks.iterator();
    }

    public BoundingBox calcBoundingBox() {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPoint block : blocks) {
            Point pos = block.pos();
            minX = Math.min(minX, pos.getIntX());
            maxX = Math.max(maxX, pos.getIntX());
            minY = Math.min(minY, pos.getIntY());
            maxY = Math.max(maxY, pos.getIntY());
            minZ = Math.min(minZ, pos.getIntZ());
            maxZ = Math.max(maxZ, pos.getIntZ());
        }

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
