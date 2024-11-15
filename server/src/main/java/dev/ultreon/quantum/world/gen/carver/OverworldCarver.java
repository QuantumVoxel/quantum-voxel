package dev.ultreon.quantum.world.gen.carver;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.HeightmapType;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.CaveNoiseGenerator;
import dev.ultreon.quantum.world.gen.HillinessNoise;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

/**
 * The OverworldCarver class is responsible for carving terrain within a chunk.
 * It uses various noise sources and domain warping to determine terrain features like caves and surface height.
 */
public class OverworldCarver implements Carver {
    private final @NotNull BlockState stoneState = Blocks.STONE.getDefaultState();
    private final DomainWarping domainWarping;
    private final NoiseSource biomeNoise;
    private final CaveNoiseGenerator caveNoise;

    public static long totalDurations = 0L;
    public static LongList durations = LongLists.synchronize(new LongArrayList());
    private final HillinessNoise hillinessNoise;

    public OverworldCarver(DomainWarping domainWarping, NoiseSource biomeNoise, long seed) {
        this.domainWarping = domainWarping;
        this.biomeNoise = biomeNoise;

        this.caveNoise = new CaveNoiseGenerator(seed);
        this.hillinessNoise = new HillinessNoise(seed + 230);
    }

    @Override
    public int carve(BuilderChunk chunk, int x, int z) {
        long start = System.currentTimeMillis();
        Vec3i offset = chunk.getOffset();
        double hilliness = this.hillinessNoise.evaluateNoise(offset.x + x, offset.z + z) - 2.0f;
        int groundPos = (int) ((this.getSurfaceHeightNoise(x, z) - 64) * (hilliness / 4.0f + 0.5f) + 64);
        int height = groundPos;
        if (height < 0) height = 0;

        // Carve the world into shape.
        for (int y = offset.y; y < offset.y + CHUNK_SIZE; y++) {
            BlockVec vec = new BlockVec(x, y, z, BlockVecSpace.WORLD).chunkLocal();
            if (y <= groundPos) {
//                if (y <= World.SEA_LEVEL) {
//                    if (y < groundPos - 7) {
//                        boolean cave;
//                        double v1 = caveNoise.evaluateNoise(x, y, z);
//                        cave = v1 > 0.0;
//                        chunk.set(vec.x, vec.y, vec.z, cave ? Blocks.CAVE_AIR.getDefaultState() : stoneState);
//                    } else {
                        chunk.set(vec.x, vec.y, vec.z, stoneState);
//                    }
//                } else {
//                    boolean cave;
//                    double v1 = caveNoise.evaluateNoise(x, y, z);
//                    cave = v1 > 0.0;
//                    chunk.set(vec.x, vec.y, vec.z, cave ? Blocks.CAVE_AIR.getDefaultState() : stoneState);
//                }
            } else if (y <= World.SEA_LEVEL) {
                chunk.set(vec.x, vec.y, vec.z, Blocks.WATER.getDefaultState());
            } else {
                chunk.set(vec.x, vec.y, vec.z, Blocks.AIR.getDefaultState());
            }
        }

        // Write to the heightmaps
        BlockVec vec = new BlockVec(x, height, z, BlockVecSpace.WORLD).chunkLocal();
        chunk.getWorld().heightMapAt(x, z, HeightmapType.WORLD_SURFACE).set(vec.x, vec.z, (short) height);
        chunk.getWorld().heightMapAt(x, z, HeightmapType.MOTION_BLOCKING).set(vec.x, vec.z, (short) height);

        // Timing for chunk carver.
        long end = System.currentTimeMillis();

        long duration = end - start;

        synchronized (this) {
            if (durations.size() > 100) {
                Long l = durations.removeFirst();
                totalDurations -= l;
            }

            totalDurations += duration;
            durations.addLast(duration);
        }

        return groundPos;
    }

    @Override
    public int getSurfaceHeightNoise(float x, float z) {
        double height;

        height = this.biomeNoise.evaluateNoise(x, z);
        return (int) Math.ceil(Math.max(height, 1));
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        double hilliness = this.hillinessNoise.evaluateNoise(x, z) - 2.0f;
        int groundPos = (int) ((this.getSurfaceHeightNoise(x, z) - 64) * (hilliness / 4.0f + 0.5f) + 64);

        if (y <= groundPos) {
            if (y <= World.SEA_LEVEL) {
                if (y < groundPos - 7) {
                    double v1 = caveNoise.evaluateNoise(x, y, z);
                    return v1 > 0.0;
                } else {
                    return false;
                }
            } else {
                double v1 = caveNoise.evaluateNoise(x, y, z);
                return v1 > 0.0;
            }
        } else return y > World.SEA_LEVEL;
    }

    public DomainWarping getDomainWarping() {
        return domainWarping;
    }
}
