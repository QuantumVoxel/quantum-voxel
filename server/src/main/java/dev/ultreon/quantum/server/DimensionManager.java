package dev.ultreon.quantum.server;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.dimension.Dimension;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages dimension and world relationships within a QuantumServer context.
 */
public class DimensionManager extends GameObject {
    private final QuantumServer server;
    private final Map<RegistryKey<DimensionInfo>, Dimension> dimensions = new HashMap<>();
    private final Map<RegistryKey<DimensionInfo>, ServerWorld> worlds = new HashMap<>();

    /**
     * Constructs a DimensionManager with the specified QuantumServer.
     *
     * @param server the QuantumServer instance associated with this DimensionManager
     */
    @ApiStatus.Internal
    public DimensionManager(QuantumServer server) {
        this.server = server;
    }

    /**
     * Retrieves the QuantumServer instance associated with this DimensionManager.
     *
     * @return the QuantumServer instance.
     */
    public QuantumServer getServer() {
        return server;
    }

    /**
     * Sets the default dimensions for the DimensionManager by populating the dimensions map with
     * pre-defined dimensions such as OVERWORLD, TEST, and SPACE using the provided ServerRegistries.
     *
     * @param registries The ServerRegistries instance containing the necessary registries for dimensions
     *                   and chunk generators.
     */
    public void setDefaults(ServerRegistries registries) {
        Registry<DimensionInfo> dimRegistry = registries.get(RegistryKeys.DIMENSION);
        Registry<ChunkGenerator> chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        this.dimensions.put(DimensionInfo.OVERWORLD, new Dimension(dimRegistry.get(DimensionInfo.OVERWORLD), chunkGenRegistry.get(ChunkGenerator.OVERWORLD)));
        this.dimensions.put(DimensionInfo.TEST, new Dimension(dimRegistry.get(DimensionInfo.TEST), chunkGenRegistry.get(ChunkGenerator.TEST)));
        this.dimensions.put(DimensionInfo.SPACE, new Dimension(dimRegistry.get(DimensionInfo.SPACE), chunkGenRegistry.get(ChunkGenerator.FLOATING_ISLANDS)));
    }

    /**
     * Loads worlds into the server based on the provided seed.
     * Iterates through the dimensions map and sets up each world with its data.
     * If an IOException occurs, it logs an error message.
     *
     * @param seed The seed value used to generate worlds if no specific seed is provided for a dimension.
     */
    public void loadWorlds(long seed) {
        for (Map.Entry<RegistryKey<DimensionInfo>, Dimension> e : this.dimensions.entrySet()) {
            RegistryKey<DimensionInfo> key = e.getKey();
            Dimension dimension = e.getValue();

            try {
                WorldStorage storage = server.getStorage();
                MapType data = new MapType();
                if (storage.exists("world.ubo"))
                    data = storage.read("world.ubo");
                ServerWorld world = new ServerWorld(server, key, storage, dimension.generator(), e.getValue().info().seed().orElse(seed ^ key.hashCode()), data);
                this.worlds.put(key, world);
                this.add("World " + key.id(), world);

                world.load();
            } catch (IOException ex) {
                QuantumServer.LOGGER.error("Failed to load server data");
            }
        }
    }

    /**
     * Loads dimensions and their respective chunk generators into the DimensionManager.
     * Iterates through the entries of the dimension registry, retrieves the corresponding
     * chunk generator for each dimension, and populates the dimensions map.
     *
     * @param registries The ServerRegistries instance containing the necessary
     *                   registries for dimensions and chunk generators.
     */
    public void load(ServerRegistries registries) {
        Registry<DimensionInfo> dimRegistry = registries.get(RegistryKeys.DIMENSION);
        Registry<ChunkGenerator> chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        for (ObjectMap.Entry<RegistryKey<DimensionInfo>, DimensionInfo> e : dimRegistry.entries()) {
            RegistryKey<DimensionInfo> key = e.key;
            DimensionInfo info = e.value;
            RegistryKey<ChunkGenerator> chunkGenKey = info.generatorKey();
            ChunkGenerator chunkGenerator = chunkGenRegistry.get(chunkGenKey);

            this.dimensions.put(key, new Dimension(info, chunkGenerator));
        }
    }

    /**
     * Retrieves a Dimension object associated with the provided registry key.
     *
     * @param key the RegistryKey of the DimensionInfo to retrieve the Dimension object for
     * @return the Dimension associated with the specified key
     */
    public Dimension get(RegistryKey<DimensionInfo> key) {
        return dimensions.get(key);
    }

    /**
     * Retrieves the ServerWorld instance associated with the provided registry key.
     *
     * @param key the RegistryKey of the DimensionInfo to retrieve the ServerWorld for
     * @return the ServerWorld associated with the specified key
     */
    public ServerWorld getWorld(RegistryKey<DimensionInfo> key) {
        return this.worlds.get(key);
    }

    /**
     * Retrieves a map of all ServerWorld instances managed by the DimensionManager,
     * keyed by their RegistryKey&lt;DimensionInfo&gt;.
     *
     * @return a map containing the RegistryKey&lt;DimensionInfo&gt; as keys and the corresponding
     * ServerWorld instances as values
     */
    public Map<RegistryKey<DimensionInfo>, ServerWorld> getWorlds() {
        return worlds;
    }
}
