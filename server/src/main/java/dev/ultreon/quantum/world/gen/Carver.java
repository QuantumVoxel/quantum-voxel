package dev.ultreon.quantum.world.gen;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockData;
import dev.ultreon.quantum.util.BlockMetaPredicate;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;

import static dev.ultreon.quantum.world.World.CHUNK_HEIGHT;

public class Carver {
    static final int HAS_CAVES_FLAG = 129;
    private final DomainWarping domainWarping;
    private final NoiseSource biomeNoise;
    private final CaveNoiseGenerator caveNoise;
    private int maxCaveHeight = 256;

    public Carver(DomainWarping domainWarping, NoiseSource biomeNoise, long seed) {
        this.domainWarping = domainWarping;
        this.biomeNoise = biomeNoise;

        this.caveNoise = new CaveNoiseGenerator(seed);
    }

    public int carve(BuilderChunk chunk, int x, int z) {
        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z);
        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            if (y <= groundPos) {
                if (y <= World.SEA_LEVEL) {
                    if (y < groundPos - 7) {
                        boolean cave;
//                    double densityFx = 64.0;
//                    double v = 1.0 - ((groundPos - densityFx) / densityFx);
//                    v *= ((groundPos - (groundPos - 7 - y))) / densityFx;
                        double v1 = caveNoise.evaluateNoise(chunk.getOffset().x + x, y, chunk.getOffset().z + z);
//                    cave = !((v - v1) > 0.0) && v1 > 0.0;
                        cave = v1 > 0.0;
                        chunk.set(x, y, z, cave ? Blocks.CAVE_AIR.createMeta() : solidBlock(y));
                    } else {
                        chunk.set(x, y, z, solidBlock(y));
                    }
                } else {
                    boolean cave;
//                    double densityFx = 64.0;
//                    double v = 1.0 - ((groundPos - densityFx) / densityFx);
//                    v *= ((groundPos - (groundPos - 7 - y))) / densityFx;
                    double v1 = caveNoise.evaluateNoise(chunk.getOffset().x + x, y, chunk.getOffset().z + z);
//                    cave = !((v - v1) > 0.0) && v1 > 0.0;
                    cave = v1 > 0.0;
                    chunk.set(x, y, z, cave ? Blocks.CAVE_AIR.createMeta() : solidBlock(y));
                }
            } else if (y <= World.SEA_LEVEL) {
                chunk.set(x, y, z, Blocks.WATER.createMeta());
            } else {
                chunk.set(x, y, z, Blocks.AIR.createMeta());
            }
        }

        return chunk.getHighest(x, z, BlockMetaPredicate.WG_HEIGHT_CHK);
    }

    private static BlockData solidBlock(int y) {
        return Blocks.STONE.createMeta();
    }

    public int getSurfaceHeightNoise(float x, float z) {
        double height;

        height = this.biomeNoise.evaluateNoise(x, z);
        return (int) Math.ceil(Math.max(height, 1));
    }
}
