package dev.ultreon.quantum.world.gen.chunk;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * The ChunkGenerator interface defines the required methods for creating and generating chunks in a server world
 * using specific parameters. Implementations should provide the logic for terrain and feature generation, along
 * with any domain warping functionalities.
 */
public interface ChunkGenerator extends Disposable {
    RegistryKey<ChunkGenerator> OVERWORLD = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new NamespaceID("overworld"));
    RegistryKey<ChunkGenerator> TEST = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new NamespaceID("test"));
    RegistryKey<ChunkGenerator> FLOATING_ISLANDS = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new NamespaceID("floating_islands"));

    /**
     * Creates a new instance of the chunk generator in the specified world, using the provided seed.
     *
     * @param world the server world in which the chunk generator is being created
     * @param seed the seed used for generating the chunk
     */
    void create(@NotNull ServerWorld world, long seed);

    /**
     * Generates terrain and features for the provided chunk in the specified world, taking into account the changes
     * made in neighboring chunks.
     *
     * @param world the server world in which the chunk is being generated
     * @param chunk the chunk to be generated
     * @param changes the collection of recorded changes in neighboring chunks
     */
    void generate(@NotNull ServerWorld world, BuilderChunk chunk, Collection<ServerWorld.@NotNull RecordedChange> changes);

    DomainWarping getLayerDomain();

    Carver getCarver();

    double getTemperature(int x, int z);
}
