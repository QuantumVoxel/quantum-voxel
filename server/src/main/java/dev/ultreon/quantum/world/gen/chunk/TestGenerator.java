package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.carver.FlatWorldCarver;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

public class TestGenerator extends SimpleChunkGenerator {
    private final FlatWorldCarver carver;

    public TestGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);

        this.carver = new FlatWorldCarver();
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, @NotNull Collection<ServerWorld.@NotNull RecordedChange> recordedChanges) {
        for (int x = chunk.getOffset().x; x < chunk.getOffset().x + CHUNK_SIZE; x++) {
            for (int z = chunk.getOffset().z; z < chunk.getOffset().z + CHUNK_SIZE; z++) {
                this.carver.carve(chunk, x, z, 0.0f);
            }
        }
    }

    @Override
    protected @NotNull Carver getCarver() {
        return carver;
    }
}
