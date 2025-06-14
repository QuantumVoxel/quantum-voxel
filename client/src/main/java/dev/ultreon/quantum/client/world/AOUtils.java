package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.world.Direction;

public class AOUtils {
    private AOUtils() {

    }

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
                calculateDirection(chunk, dir, point, array);
            }
        }
        
        return array;
    }

    private static void calculateDirection(ClientChunkAccess chunk, Direction dir, Vector3 point, int[] array) {
        int ao;
        switch (dir.getAxis()) {
            case Y:
                ao = calculateYAxis(chunk, dir, point);
                break;
            case X:
                ao = calculateXAxis(chunk, dir, point);
                break;
            case Z:
                ao = calculateZAxis(chunk, point);
                break;
            default:
                ao = createAO(false, false, false, false);
                break;
        }
        array[dir.ordinal()] = ao;
    }

    private static int calculateZAxis(ClientChunkAccess chunk, Vector3 point) {
        int ao;
        BlockModel westUp = getModelAt(chunk, (int) point.x - 1, (int) point.y + 1, (int) point.z);
        BlockModel up1 = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z);
        BlockModel west1 = getModelAt(chunk, (int) point.x - 1, (int) point.y, (int) point.z);
        BlockModel eastUp = getModelAt(chunk, (int) point.x + 1, (int) point.y + 1, (int) point.z);
        BlockModel east1 = getModelAt(chunk, (int) point.x + 1, (int) point.y, (int) point.z);
        BlockModel eastDown = getModelAt(chunk, (int) point.x + 1, (int) point.y - 1, (int) point.z);
        BlockModel down1 = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z);
        BlockModel westDown = getModelAt(chunk, (int) point.x - 1, (int) point.y - 1, (int) point.z);
        ao = createAO(
                westDown.hasAO() || down1.hasAO() || west1.hasAO(),
                westUp.hasAO() || up1.hasAO() || west1.hasAO(),
                eastDown.hasAO() || down1.hasAO() || east1.hasAO(),
                eastUp.hasAO() || up1.hasAO() || east1.hasAO()
        );
        return ao;
    }

    private static int calculateXAxis(ClientChunkAccess chunk, Direction dir, Vector3 point) {
        int ao;
        BlockModel northUp = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z + 1);
        BlockModel up = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z);
        BlockModel north1 = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z + 1);
        BlockModel southUp = getModelAt(chunk, (int) point.x, (int) point.y + 1, (int) point.z - 1);
        BlockModel south1 = getModelAt(chunk, (int) point.x, (int) point.y, (int) point.z - 1);
        BlockModel southDown = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z - 1);
        BlockModel down = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z);
        BlockModel northDown = getModelAt(chunk, (int) point.x, (int) point.y - 1, (int) point.z + 1);
        if (dir.isNegative()) {
            ao = createAO(
                    southDown.hasAO() || down.hasAO() || south1.hasAO(),
                    southUp.hasAO() || up.hasAO() || south1.hasAO(),
                    northDown.hasAO() || down.hasAO() || north1.hasAO(),
                    northUp.hasAO() || up.hasAO() || north1.hasAO()
            );
        } else {
            ao = createAO(
                    northDown.hasAO() || down.hasAO() || north1.hasAO(),
                    northUp.hasAO() || up.hasAO() || north1.hasAO(),
                    southDown.hasAO() || down.hasAO() || south1.hasAO(),
                    southUp.hasAO() || up.hasAO() || south1.hasAO()
            );
        }
        return ao;
    }

    private static int calculateYAxis(ClientChunkAccess chunk, Direction dir, Vector3 point) {
        int ao;
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
        return ao;
    }

    private static BlockModel getModelAt(ClientChunkAccess chunk, int x, int y, int z) {
        BlockState state = chunk.getSafe(x, y, z);
        return QuantumClient.get().getBlockModel(state);
    }
}
