package dev.ultreon.quantum.world.gen.feature;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.world.Fork;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FoliageFeature extends TerrainFeature {
    private final Block material;
    private final float threshold;
    private final Set<Block> validGround;
    private final Random random = new Random();

    public FoliageFeature(Block material, float threshold, Block... validGround) {
        super();

        this.material = material;
        this.threshold = threshold;
        this.validGround = new HashSet<>(List.of(validGround));
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        this.random.setSeed(seed);

        BlockState under = setter.get(x, y, z);
        if (validGround.contains(under.getBlock())
                && this.random.nextFloat() < this.threshold) {
            setter.set(x, y + 1, z, this.material.getDefaultState());
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}
