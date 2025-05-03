package dev.ultreon.quantum.util;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.ChunkReader;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.WorldReader;

import static dev.ultreon.quantum.world.World.CS;

@SuppressWarnings("UnqualifiedStaticUsage")
public class WorldRayCaster {
    private static final Vec3i abs = new Vec3i();
    private static final Vec3i origin = new Vec3i();
    private static final Vec3i loc = new Vec3i();
    private static final Vec3d dir = new Vec3d();
    private static final Vec3d ext = new Vec3d();
    private static final Vec3d intersection = new Vec3d();
    private static final Vec3d local = new Vec3d();
    private static final BoundingBox box = new BoundingBox();

    public static BlockHit rayCast(WorldReader map) {
        return rayCast(new BlockHit(), map);
    }

    // sources : https://www.researchgate.net/publication/2611491_A_Fast_Voxel_Traversal_Algorithm_for_Ray_Tracing
    // and https://www.gamedev.net/blogs/entry/2265248-voxel-traversal-algorithm-ray-casting/
    public static BlockHit rayCast(BlockHit result, WorldReader world) {
        return rayCast(result, world, BlockMetaPredicate.NON_FLUID);
    }

    // sources : https://www.researchgate.net/publication/2611491_A_Fast_Voxel_Traversal_Algorithm_for_Ray_Tracing
    // and https://www.gamedev.net/blogs/entry/2265248-voxel-traversal-algorithm-ray-casting/
    public static BlockHit rayCast(BlockHit result, WorldReader world, BlockMetaPredicate predicate) {
        result.collide = false;

        final Ray ray = result.ray;

        dir.set(ray.direction.x > 0 ? 1 : -1,
                ray.direction.y > 0 ? 1 : -1,
                ray.direction.z > 0 ? 1 : -1);
        ext.set(ray.direction.x > 0 ? 1 : 0,
                ray.direction.y > 0 ? 1 : 0,
                ray.direction.z > 0 ? 1 : 0);

        ChunkReader chunk = null;

        origin.set((int) Math.floor(ray.origin.x), (int) Math.floor(ray.origin.y), (int) Math.floor(ray.origin.z));
        abs.set(origin);

        final double nextX = abs.x + ext.x;
        final double nextY = abs.y + ext.y;
        final double nextZ = abs.z + ext.z;

        double tMaxX = ray.direction.x == 0 ? Float.MAX_VALUE : (nextX - ray.origin.x) / ray.direction.x;
        double tMaxY = ray.direction.y == 0 ? Float.MAX_VALUE : (nextY - ray.origin.y) / ray.direction.y;
        double tMaxZ = ray.direction.z == 0 ? Float.MAX_VALUE : (nextZ - ray.origin.z) / ray.direction.z;


        final double tDeltaX = ray.direction.x == 0 ? Float.MAX_VALUE : dir.x / ray.direction.x;
        final double tDeltaY = ray.direction.y == 0 ? Float.MAX_VALUE : dir.y / ray.direction.y;
        final double tDeltaZ = ray.direction.z == 0 ? Float.MAX_VALUE : dir.z / ray.direction.z;


        for (; ; ) {
            if (abs.dst(origin) > result.distanceMax) return result;

            if (chunk == null || chunk.isDisposed()) {
                chunk = world.getChunkAt(abs.x, abs.y, abs.z);
                if (chunk == null || chunk.isDisposed()) return result;
            }

            loc.set(abs).sub(chunk.getOffset().x, chunk.getOffset().y, chunk.getOffset().z);

            if (loc.x < 0 || loc.y < 0 || loc.z < 0 ||
                loc.x >= CS || loc.y >= CS || loc.z >= CS) {
                chunk = null;
                continue;
            }

            BlockState blockState = chunk.get(loc.x, loc.y, loc.z);
            if (blockState != null && !blockState.isAir() && predicate.test(blockState)) {
                Block block = blockState.getBlock();
                block.boundingBox(abs.x, abs.y, abs.z, blockState, box);
                box.update();

                doIntersect(result, ray, blockState);

                return result;
            }

            // increment
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    tMaxX += tDeltaX;
                    abs.x += (int) dir.x;
                } else {
                    tMaxZ += tDeltaZ;
                    abs.z += (int) dir.z;
                }
            } else if (tMaxY < tMaxZ) {
                tMaxY += tDeltaY;
                abs.y += (int) dir.y;
            } else {
                tMaxZ += tDeltaZ;
                abs.z += (int) dir.z;
            }
        }
    }

    private static void doIntersect(BlockHit result, Ray ray, BlockState block) {
        if (Intersector.intersectRayBounds(ray, box, intersection)) {
            double dst = intersection.dst(ray.origin);
            result.collide = true;
            result.distance = (float) dst;
            result.position.set(intersection);
            result.vec.set(abs);
            result.blockMeta = block;
            result.block = block.getBlock();

            computeFace(result);
        }
    }


    private static void computeFace(BlockHit result) {
        // compute face
        local.set(result.position)
                .sub(result.vec.x, result.vec.y, result.vec.z)
                .sub(.5f);

        double absX = Math.abs(local.x);
        double absY = Math.abs(local.y);
        double absZ = Math.abs(local.z);

        if (absY > absX) {
            if (absZ > absY) {
                // face Z+
                result.normal.set(0, 0, local.z < 0 ? -1 : 1);
                result.direction = local.z < 0 ? Direction.WEST : Direction.EAST;
            } else {
                result.normal.set(0, local.y < 0 ? -1 : 1, 0);
                result.direction = local.y < 0 ? Direction.DOWN : Direction.UP;
            }
        } else {
            if (absZ > absX) {
                result.normal.set(0, 0, local.z < 0 ? -1 : 1);
                result.direction = local.z < 0 ? Direction.WEST : Direction.EAST;
            } else {
                result.normal.set(local.x < 0 ? -1 : 1, 0, 0);
                result.direction = local.x < 0 ? Direction.SOUTH : Direction.NORTH;
            }
        }
    }
}