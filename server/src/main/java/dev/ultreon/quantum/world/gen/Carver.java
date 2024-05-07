package dev.ultreon.quantum.world.gen;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.util.BlockMetaPredicate;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;

import static dev.ultreon.quantum.world.World.CHUNK_HEIGHT;

public class Carver {
    private final DomainWarping domainWarping;
    private final NoiseSource biomeNoise;
    private final CaveNoiseGenerator caveNoise;

    public Carver(DomainWarping domainWarping, NoiseSource biomeNoise, long seed) {
        this.domainWarping = domainWarping;
        this.biomeNoise = biomeNoise;

        this.caveNoise = new CaveNoiseGenerator(seed);
    }

    public int carve(BuilderChunk chunk, int x, int z) {
        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z);
        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            if (y <= groundPos) {
                double noise = this.caveNoise.evaluateNoise((chunk.getOffset().x + x), y, (chunk.getOffset().z + z));
                chunk.set(x, y, z, noise == 1.0 ? Blocks.CAVE_AIR.createMeta() : solidBlock(y));
            } else if (y <= World.SEA_LEVEL) {
                chunk.set(x, y, z, Blocks.WATER.createMeta());
            } else {
                chunk.set(x, y, z, Blocks.AIR.createMeta());
            }
        }

        return chunk.getHighest(x, z, BlockMetaPredicate.WG_HEIGHT_CHK);
    }

    private static BlockProperties solidBlock(int y) {
        return Blocks.STONE.createMeta();
    }

    public int getSurfaceHeightNoise(float x, float z) {
        double height;

        height = this.biomeNoise.evaluateNoise(x, z);
        return (int) Math.ceil(Math.max(height, 1));
    }
}
