package dev.ultreon.quantum.world.gen.feature;

import com.badlogic.gdx.math.GridPoint2;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.gen.WorldGenFeature;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import kotlin.ranges.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OreFeature extends WorldGenFeature {
    private final Block ore;
    private final int chance;
    private final long seed;
    private final IntRange sizeRange;
    private final IntRange heightRange;

    public OreFeature(long seed, Block ore, int chance, IntRange sizeRange, IntRange heightRange) {
        super();
        this.seed = seed;

        this.ore = ore;
        this.chance = chance;
        this.sizeRange = sizeRange;
        this.heightRange = heightRange;
    }

    @Override
    public boolean handle(@NotNull World world, @NotNull ChunkAccess chunk, int x, int z, int height) {
        int posSeed = (x + chunk.getOffset().x) << 16 | (z + chunk.getOffset().z) & 0xFFFF;
        long seed = (world.getSeed() ^ this.seed << 32) ^ posSeed;

        RNG random = new JavaRNG(seed);

        if (height < this.heightRange.getStart()) return false;

        if (random.chance(this.chance)) {
            int y = random.randint(heightRange.getStart(), heightRange.getEndInclusive());

            if (chunk.get(x, y, z).getBlock() != Blocks.STONE) return false;

            int v = random.randint(sizeRange.getStart(), sizeRange.getEndInclusive());
            int xOffset = 0;
            int zOffset = 0;

            chunk.set(x + xOffset, y, z + zOffset, this.ore.createMeta());

            if (DebugFlags.ORE_FEATURE.isEnabled()) {
                QuantumServer.LOGGER.warn("Generating ore feature at: " + (x + chunk.getOffset().x) + ", " + (y + chunk.getOffset().y) + ", " + (z + chunk.getOffset().z));
            }

            GridPoint2 offset = new GridPoint2(xOffset, zOffset);
            List<GridPoint2> offsets = new DefaultedArray<>(() -> new GridPoint2(0, 0), v);
            for (int i = 0; i < v; i++) {
                int attempts = 3;
                while (offsets.contains(offset) && attempts-- > 0) {
                    xOffset = random.randint(-(v / 2) - 1, (v / 2) + 1);
                    zOffset = random.randint(-(v / 2) - 1, (v / 2) + 1);
                    offset = new GridPoint2(xOffset, zOffset);
                }

                offsets.add(offset);

                if (chunk.get(x + xOffset, y, z + zOffset).getBlock() != Blocks.STONE) continue;
                chunk.set(x + xOffset, y, z + zOffset, this.ore.createMeta());
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}
