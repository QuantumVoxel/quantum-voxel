package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.carver.FlatWorldCarver;
import org.jetbrains.annotations.NotNull;

import static dev.ultreon.quantum.world.World.CS;

public class TestGenerator extends SimpleChunkGenerator {
    private final FlatWorldCarver carver;

    public TestGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);

        this.carver = new FlatWorldCarver();
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver) {
        for (int x = chunk.getOffset().x; x < chunk.getOffset().x + CS; x++) {
            for (int z = chunk.getOffset().z; z < chunk.getOffset().z + CS; z++) {
                this.carver.carve(chunk, x, z);
            }
        }
    }

    @Override
    @NotNull
    public Carver getCarver() {
        return carver;
    }

    @Override
    public double getTemperature(int x, int z) {
        return 1.1;
    }
}
