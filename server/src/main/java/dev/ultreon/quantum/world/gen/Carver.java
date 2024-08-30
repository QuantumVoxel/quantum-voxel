package dev.ultreon.quantum.world.gen;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.HeightmapType;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;
import static java.lang.Math.min;

public class Carver {
    private final DomainWarping domainWarping;
    private final NoiseSource biomeNoise;
    private final CaveNoiseGenerator caveNoise;

    public static long totalDurations = 0L;
    public static LongList durations = LongLists.synchronize(new LongArrayList());

    public Carver(DomainWarping domainWarping, NoiseSource biomeNoise, long seed) {
        this.domainWarping = domainWarping;
        this.biomeNoise = biomeNoise;

        this.caveNoise = new CaveNoiseGenerator(seed);
    }

    public int carve(BuilderChunk chunk, int x, int z, double hilliness) {
        long start = System.currentTimeMillis();
        Vec3i offset = chunk.getOffset();
        int groundPos = (int) ((this.getSurfaceHeightNoise(x, z) - 64) * (hilliness / 4.0f + 0.5f) + 64);
        int height = groundPos;
        if (height < 0) height = 0;

        for (int y = offset.y; y < offset.y + CHUNK_SIZE; y++) {
            if (y <= groundPos) {
                if (y <= World.SEA_LEVEL) {
                    if (y < groundPos - 7) {
                        boolean cave;
                        double v1 = caveNoise.evaluateNoise(x, y, z);
                        cave = v1 > 0.0;
                        chunk.set(x, y, z, cave ? Blocks.CAVE_AIR.createMeta() : solidBlock(y));
                    } else {
                        chunk.set(x, y, z, solidBlock(y));
                    }
                } else {
                    boolean cave;
                    double v1 = caveNoise.evaluateNoise(x, y, z);
                    cave = v1 > 0.0;
                    chunk.set(x, y, z, cave ? Blocks.CAVE_AIR.createMeta() : solidBlock(y));
                    height = min(height, y - 1);
                }
            } else if (y <= World.SEA_LEVEL) {
                chunk.set(x, y, z, Blocks.WATER.createMeta());
            } else {
                chunk.set(x, y, z, Blocks.AIR.createMeta());
            }
        }

        BlockVec vec = new BlockVec(x, height, z, BlockVecSpace.WORLD).chunkLocal();
        chunk.getWorld().heightMapAt(x, z, HeightmapType.WORLD_SURFACE).set(vec.x, vec.z, (short) height);

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

    private static BlockState solidBlock(int y) {
        return Blocks.STONE.createMeta();
    }

    public int getSurfaceHeightNoise(float x, float z) {
        double height;

        height = this.biomeNoise.evaluateNoise(x, z);
        return (int) Math.ceil(Math.max(height, 1));
    }

    public DomainWarping getDomainWarping() {
        return domainWarping;
    }
}
