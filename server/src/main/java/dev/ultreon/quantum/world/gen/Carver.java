package dev.ultreon.quantum.world.gen;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.HeightmapType;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;

import static dev.ultreon.quantum.world.World.CHUNK_HEIGHT;

public class Carver {
    static final int HAS_CAVES_FLAG = 129;
    private final DomainWarping domainWarping;
    private final NoiseSource biomeNoise;
    private final CaveNoiseGenerator caveNoise;

    public Carver(DomainWarping domainWarping, NoiseSource biomeNoise, long seed) {
        this.domainWarping = domainWarping;
        this.biomeNoise = biomeNoise;

        this.caveNoise = new CaveNoiseGenerator(seed);
    }

    public int carve(BuilderChunk chunk, int x, int z, double hilliness) {
        int groundPos = (int) ((this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z) - 64) * (hilliness / 4.0f + 0.5f) + 64);
        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            if (y <= groundPos) {
                if (y <= World.SEA_LEVEL) {
                    if (y < groundPos - 7) {
                        boolean cave;
                        double v1 = caveNoise.evaluateNoise(chunk.getOffset().x + x, y, chunk.getOffset().z + z);
                        cave = v1 > 0.0;
                        chunk.set(x, y, z, cave ? Blocks.CAVE_AIR.createMeta() : solidBlock(y));
                    } else {
                        chunk.set(x, y, z, solidBlock(y));
                    }
                } else {
                    boolean cave;
                    double v1 = caveNoise.evaluateNoise(chunk.getOffset().x + x, y, chunk.getOffset().z + z);
                    cave = v1 > 0.0;
                    chunk.set(x, y, z, cave ? Blocks.CAVE_AIR.createMeta() : solidBlock(y));
                }
            } else {
                chunk.set(x, y, z, Blocks.AIR.createMeta());
            }
        }

        return chunk.getHeight(x, z, HeightmapType.WORLD_SURFACE);
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
