package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.world.Direction;

public class AOUtils {
    private static final Color DEEP_AO = new Color(0f, 0f, 0f, 0.8f);
    private static final Color LIGHT_AO = new Color(0f, 0f, 0f, 0.4f);
    private static final Color NO_AO = new Color(0f, 0f, 0f, 0f);

    // AO Arrays
    public static boolean hasAO(int[] ao) {
        for (int i : ao) if (i != 0) return true;
        return false;
    }

    public static int aoForSide(int[] ao, Direction direction) {
        return ao[direction.ordinal()];
    }

    public static int get(int[] ao, int index) {
        return ao[index];
    }

    public static void set(int[] ao, int index, int value) {
        ao[index] = value;
    }

    // AO Values
    public static boolean hasAO(int ao) {
        return ao != 0;
    }

    public static boolean hasAoCorner00(int ao) {
        return (ao & 1) != 0;
    }

    public static boolean hasAoCorner01(int ao) {
        return (ao & 2) != 0;
    }

    public static boolean hasAoCorner10(int ao) {
        return (ao & 4) != 0;
    }

    public static boolean hasAoCorner11(int ao) {
        return (ao & 8) != 0;
    }

    public static String toString(int ao) {
        return "AO{" +
                "[00]=" + hasAoCorner00(ao) +
                ", [01]=" + hasAoCorner01(ao) +
                ", [10]=" + hasAoCorner10(ao) +
                ", [11]=" + hasAoCorner11(ao) +
                '}';
    }

    public static int createAO(boolean corner00, boolean corner01, boolean corner10, boolean corner11) {
        return (corner00 ? 1 : 0) | (corner01 ? 2 : 0) | (corner10 ? 4 : 0) | (corner11 ? 8 : 0);
    }

    public static int flipped(int ao) {
        return createAO(
                hasAoCorner11(ao),
                hasAoCorner10(ao),
                hasAoCorner01(ao),
                hasAoCorner00(ao)
        );
    }

    public static int[] calculate(ClientChunkAccess chunk, int x, int y, int z) {
        if (!getModelAt(chunk, x, y, z).hasAO()) {
            return new int[]{0, 0, 0, 0, 0, 0};
        }
        int[] array = new int[6];
        for (Direction dir : Direction.values()) {
            Vector3 point = new Vector3(x + dir.getNormal().x, y + dir.getNormal().y, z + dir.getNormal().z);
            BlockModel block = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z);
            if (!block.hasAO()) {
                int ao;
                switch (dir.getAxis()) {
                    case Y -> {
                        BlockModel northWest = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z - 1);
                        BlockModel west = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z);
                        BlockModel north = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z - 1);
                        BlockModel southWest = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z + 1);
                        BlockModel south = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z + 1);
                        BlockModel southEast = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z + 1);
                        BlockModel east = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z);
                        BlockModel northEast = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z - 1);
                        if (dir.isNegative()) {
                            ao = createAO(
                                    northWest.hasAO() || west.hasAO() || north.hasAO(),
                                    southWest.hasAO() || west.hasAO() || south.hasAO(),
                                    northEast.hasAO() || east.hasAO() || north.hasAO(),
                                    southEast.hasAO() || east.hasAO() || south.hasAO()
                            );
                        } else {
                            ao = flipped(createAO(
                                    northEast.hasAO() || east.hasAO() || north.hasAO(),
                                    southEast.hasAO() || east.hasAO() || south.hasAO(),
                                    northWest.hasAO() || west.hasAO() || north.hasAO(),
                                    southWest.hasAO() || west.hasAO() || south.hasAO()
                            ));
                        }
                    }
                    case X -> {
                        BlockModel northUp = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z + 1);
                        BlockModel up = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z);
                        BlockModel north = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z + 1);
                        BlockModel southUp = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z - 1);
                        BlockModel south = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z - 1);
                        BlockModel southDown = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z - 1);
                        BlockModel down = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z);
                        BlockModel northDown = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z + 1);
                        if (dir.isNegative()) {
                            ao = createAO(
                                    southDown.hasAO() || down.hasAO() || south.hasAO(),
                                    southUp.hasAO() || up.hasAO() || south.hasAO(),
                                    northDown.hasAO() || down.hasAO() || north.hasAO(),
                                    northUp.hasAO() || up.hasAO() || north.hasAO()
                            );
                        } else {
                            ao = createAO(
                                    northDown.hasAO() || down.hasAO() || north.hasAO(),
                                    northUp.hasAO() || up.hasAO() || north.hasAO(),
                                    southDown.hasAO() || down.hasAO() || south.hasAO(),
                                    southUp.hasAO() || up.hasAO() || south.hasAO()
                            );
                        }
                    }
                    case Z -> {
                        BlockModel westUp = getModelAt(chunk, (int) point.x - 1, (int) point.y + 1, (int) point.z);
                        BlockModel up = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z);
                        BlockModel west = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z);
                        BlockModel eastUp = getModelAt(chunk, (int) point.x + 1, (int) point.y + 1, (int) point.z);
                        BlockModel east = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z);
                        BlockModel eastDown = getModelAt(chunk, (int) point.x + 1, (int) point.y - 1, (int) point.z);
                        BlockModel down = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z);
                        BlockModel westDown = getModelAt(chunk, (int) point.x - 1, (int) point.y - 1, (int) point.z);
                        ao = createAO(
                                westDown.hasAO() || down.hasAO() || west.hasAO(),
                                westUp.hasAO() || up.hasAO() || west.hasAO(),
                                eastDown.hasAO() || down.hasAO() || east.hasAO(),
                                eastUp.hasAO() || up.hasAO() || east.hasAO()
                        );
                    }
                    default -> ao = createAO(false, false, false, false);
                }
                array[dir.ordinal()] = ao;
            }
        }
        
        return array;
    }

    public static BlockModel getModelAt(ClientChunkAccess chunk, int x, int y, int z) {
        BlockState state = chunk.getSafe(x, y, z);
        return QuantumClient.get().getBlockModel(state);
    }

    public static Color getAoCorner00(int ao) {
        if (hasAoCorner00(ao)) {
            if (hasAoCorner01(ao) && hasAoCorner10(ao)) {
                return DEEP_AO;
            }

            return LIGHT_AO;
        }

        return NO_AO;
    }

    public static Color getAoCorner01(int ao) {
        if (hasAoCorner01(ao)) {
            if (hasAoCorner00(ao) && hasAoCorner11(ao)) {
                return DEEP_AO;
            }

            return LIGHT_AO;
        }

        return NO_AO;
    }

    public static Color getAoCorner10(int ao) {
        if (hasAoCorner10(ao)) {
            if (hasAoCorner00(ao) && hasAoCorner11(ao)) {
                return DEEP_AO;
            }

            return LIGHT_AO;
        }

        return NO_AO;
    }

    public static Color getAoCorner11(int ao) {
        if (hasAoCorner11(ao)) {
            if (hasAoCorner00(ao) && hasAoCorner10(ao)) {
                return DEEP_AO;
            }

            return LIGHT_AO;
        }

        return NO_AO;
    }
}
