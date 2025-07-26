package dev.ultreon.quantum.client.world;

import dev.ultreon.quantum.world.Direction;

import java.util.BitSet;

public class OpaqueFaces {
    private final BitSet[] opaqueFaces = new BitSet[Direction.values().length];
    
    public OpaqueFaces() {
        for (int i = 0; i < this.opaqueFaces.length; i++) {
            this.opaqueFaces[i] = new BitSet();
        }
    }

    public void add(int x, int y, int z, Direction direction) {
        switch (direction) {
            case UP:
            case DOWN:
                this.opaqueFaces[direction.ordinal()].set(x * ClientWorld.CS + z);
                break;
            case NORTH:
            case SOUTH:
                this.opaqueFaces[direction.ordinal()].set(x * ClientWorld.CS + y);
                break;
            case EAST:
            case WEST:
                this.opaqueFaces[direction.ordinal()].set(y * ClientWorld.CS + z);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public boolean isFull(Direction direction) {
        BitSet bitSet = this.opaqueFaces[ direction.ordinal()];
        return bitSet != null && bitSet.nextClearBit(0) == (ClientWorld.CS - 1) * ClientWorld.CS + (ClientWorld.CS - 1);
    }

    public void clear() {
        for (BitSet bitSet : this.opaqueFaces) {
            bitSet.clear();
        }
    }
}
