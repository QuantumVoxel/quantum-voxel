package dev.ultreon.quantum.server;

import dev.ultreon.quantum.registry.*;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ServerRegistries {
    private final QuantumServer server;

    public final ServerRegistry<Registry<?>> registries;
    private final ServerRegistry<Biome> biomes;
    private final ServerRegistry<ChunkGenerator> chunkGenerators;
    private final ServerRegistry<DimensionInfo> dimensions;
    private final ServerRegistry<NoiseConfig> noiseConfigs;

    public ServerRegistries(QuantumServer server) {
        this.server = server;

        registries = ServerRegistry.<Registry<?>>builder(server, NamespaceID.tryParse("server_registry")).build();
        biomes = create(RegistryKeys.BIOME);
        chunkGenerators = create(RegistryKeys.CHUNK_GENERATOR);
        dimensions = create(RegistryKeys.DIMENSION);
        noiseConfigs = create(RegistryKeys.NOISE_CONFIG);

        biomes.createTag(new NamespaceID("overworld_biomes"));
    }

    public ServerRegistry<Biome> biomes() {
        return biomes;
    }

    public ServerRegistry<ChunkGenerator> chunkGenerators() {
        return chunkGenerators;
    }

    public ServerRegistry<DimensionInfo> dimensions() {
        return dimensions;
    }

    public ServerRegistry<NoiseConfig> noiseConfigs() {
        return noiseConfigs;
    }

    @SafeVarargs
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final <T> ServerRegistry<T> create(RegistryKey<Registry<T>> key, T... typeGetter) {
        ServerRegistry<T> registry = ServerRegistry.builder(server, key.id(), typeGetter).build();
        registries.register((RegistryKey) key, registry);
        return registry;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> ServerRegistry<T> get(RegistryKey<Registry<T>> registryKey) {
        return (ServerRegistry<T>) registries.get((RegistryKey) registryKey);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Registry<T> getOrGeneric(RegistryKey<Registry<T>> registryKey) {
        ServerRegistry<T> tServerRegistry = (ServerRegistry<T>) registries.get((RegistryKey) registryKey);
        if (tServerRegistry == null) {
            return (Registry<T>) Registries.REGISTRY.get(registryKey.id());
        }
        return tServerRegistry;
    }

    @SuppressWarnings("unchecked")
    public Stream<ServerRegistry<?>> stream() {
        return Arrays.stream(registries.values().toArray().toArray(ServerRegistry.class)).filter(Objects::nonNull).map(ServerRegistry.class::cast);
    }
}
