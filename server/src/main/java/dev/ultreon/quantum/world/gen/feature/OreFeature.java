package dev.ultreon.quantum.world.gen.feature;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.world.CubicDirection;
import dev.ultreon.quantum.world.Fork;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import kotlin.ranges.IntRange;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class OreFeature extends TerrainFeature {
    private final Block ore;
    private final int chance;
    private final IntRange sizeRange;
    private final IntRange heightRange;

    public OreFeature(Block ore, int chance, IntRange sizeRange, IntRange heightRange) {
        super();

        this.ore = ore;
        this.chance = chance * CHUNK_SIZE;
        this.sizeRange = sizeRange;
        this.heightRange = heightRange;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, @NotNull BlockState origin) {
        return heightRange.contains(y) && origin.getBlock() == Blocks.STONE;
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        RNG random = new JavaRNG(seed);

        if (random.chance(this.chance)) {
            int size = random.randint(sizeRange.getStart(), sizeRange.getEndInclusive());
            setter.set(x, y, z, this.ore.createMeta());

            if (DebugFlags.ORE_FEATURE.isEnabled()) {
                QuantumServer.LOGGER.warn("Generating ore feature at: " + x + ", " + y + ", " + z);
            }

            CubicDirection dir = CubicDirection.random(random);
            BlockVec vec = new BlockVec(0, 0, 0, BlockVecSpace.WORLD);
            for (int i = 0; i < size; i++) {
                vec = vec.relative(dir);
                if (!setter.isAir(x, y, z))
                    setter.set(vec.x, vec.y, vec.z, this.ore.createMeta());
                if (random.chance(.5f))
                    dir = CubicDirection.random(random);
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}
