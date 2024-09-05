package dev.ultreon.quantum.server;

import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.dimension.Dimension;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import dev.ultreon.ubo.types.MapType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DimensionManager {
    private final QuantumServer server;
    private final Map<RegistryKey<DimensionInfo>, Dimension> dimensions = new HashMap<>();
    private final Map<RegistryKey<DimensionInfo>, ServerWorld> worlds = new HashMap<>();

    public DimensionManager(QuantumServer server) {
        this.server = server;
    }

    public QuantumServer getServer() {
        return server;
    }

    public void setDefaults(ServerRegistries registries) {
        Registry<DimensionInfo> dimRegistry = registries.get(RegistryKeys.DIMENSION);
        Registry<ChunkGenerator> chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        this.dimensions.put(DimensionInfo.OVERWORLD, new Dimension(dimRegistry.get(DimensionInfo.OVERWORLD), chunkGenRegistry.get(ChunkGenerator.OVERWORLD)));
        this.dimensions.put(DimensionInfo.TEST, new Dimension(dimRegistry.get(DimensionInfo.TEST), chunkGenRegistry.get(ChunkGenerator.TEST)));
    }

    public void loadWorlds() {
        for (Map.Entry<RegistryKey<DimensionInfo>, Dimension> e : this.dimensions.entrySet()) {
            RegistryKey<DimensionInfo> key = e.getKey();
            Dimension dimension = e.getValue();

            try {
                WorldStorage storage = server.getStorage();
                MapType data = new MapType();
                if (storage.exists("world.ubo"))
                    data = storage.read("world.ubo");
                ServerWorld world = new ServerWorld(server, key, storage, dimension.generator(), data);
                this.worlds.put(key, world);

                world.load();
            } catch (IOException ex) {
                QuantumServer.LOGGER.error("Failed to load server data");
            }
        }
    }

    public void load(ServerRegistries registries) {
        Registry<DimensionInfo> dimRegistry = registries.get(RegistryKeys.DIMENSION);
        Registry<ChunkGenerator> chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        for (Map.Entry<RegistryKey<DimensionInfo>, DimensionInfo> e : dimRegistry.entries()) {
            RegistryKey<DimensionInfo> key = e.getKey();
            DimensionInfo info = e.getValue();
            RegistryKey<ChunkGenerator> chunkGenKey = info.generatorKey();
            ChunkGenerator chunkGenerator = chunkGenRegistry.get(chunkGenKey);

            this.dimensions.put(key, new Dimension(info, chunkGenerator));
        }
    }

    public Dimension get(RegistryKey<DimensionInfo> key) {
        return dimensions.get(key);
    }

    public ServerWorld getWorld(RegistryKey<DimensionInfo> key) {
        return this.worlds.get(key);
    }

    public Map<RegistryKey<DimensionInfo>, ServerWorld> getWorlds() {
        return worlds;
    }
}
