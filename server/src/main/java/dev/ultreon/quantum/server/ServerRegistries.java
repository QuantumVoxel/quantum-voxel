package dev.ultreon.quantum.server;

import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.registry.*;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ServerRegistries implements RegistryHandle {

    public final Registry<Registry<?>> registries;
    private final Registry<Biome> biomes;
    private final Registry<ChunkGenerator> chunkGenerators;
    private final Registry<DimensionInfo> dimensions;
    private final Registry<NoiseConfig> noiseConfigs;

    public ServerRegistries(QuantumServer server) {

        registries = SimpleRegistry.<Registry<?>>builder(NamespaceID.tryParse("server_registry")).build();
        biomes = create(RegistryKeys.BIOME);
        chunkGenerators = create(RegistryKeys.CHUNK_GENERATOR);
        dimensions = create(RegistryKeys.DIMENSION);
        noiseConfigs = create(RegistryKeys.NOISE_CONFIG);

        biomes.createTag(new NamespaceID("overworld_biomes"));
    }

    public Registry<Biome> biomes() {
        return biomes;
    }

    public Registry<ChunkGenerator> chunkGenerators() {
        return chunkGenerators;
    }

    public Registry<DimensionInfo> dimensions() {
        return dimensions;
    }

    public Registry<NoiseConfig> noiseConfigs() {
        return noiseConfigs;
    }

    @SafeVarargs
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final <T> Registry<T> create(RegistryKey<Registry<T>> key, T... typeGetter) {
        Registry<T> registry = SimpleRegistry.builder(key.id(), typeGetter).build();
        registries.register((RegistryKey) key, registry);
        return registry;
    }

    public final void sendRegistries(IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        connection.send(new S2CRegistriesSync(registries));

        for (Registry<?> registry : registries.values()) {
            registry.send(connection);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Registry<T> get(RegistryKey<? extends Registry<T>> registryKey) {
        return (Registry<T>) registries.get((RegistryKey) registryKey);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Registry<T> getOrGeneric(RegistryKey<Registry<T>> registryKey) {
        Registry<T> tRegistry = (Registry<T>) registries.get((RegistryKey) registryKey);
        if (tRegistry == null) {
            return (Registry<T>) Registries.REGISTRY.get(registryKey.id());
        }
        return tRegistry;
    }

    @SuppressWarnings("unchecked")
    public Stream<Registry<?>> stream() {
        return Arrays.stream(registries.values().toArray().toArray(Registry.class)).filter(Objects::nonNull).map(Registry.class::cast);
    }
}
