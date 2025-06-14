package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.util.NamespaceID;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the registry of block entity models.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class BlockEntityModelRegistry {
    private static final Map<BlockEntityType<?>, Function<NamespaceID, BlockModel>> REGISTRY = new HashMap<>();
    private static final Map<BlockEntityType<?>, BlockModel> FINISHED_REGISTRY = new HashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private BlockEntityModelRegistry() {

    }

    /**
     * Registers a block entity model.
     *
     * @param type the type of the block entity.
     * @param modelFactory the factory of the model.
     */
    public static <T extends BlockEntity> void register(BlockEntityType<T> type, Function<NamespaceID, BlockModel> modelFactory) {
        REGISTRY.put(type, modelFactory);
    }

    /**
     * Loads the block entity models.
     * NOTE: Internal method. Do not call this method unless you know what you are doing.
     *
     * @param client the client.
     */
    @Internal
    public static void load(QuantumClient client) {
        for (var entry : REGISTRY.entrySet()) {
            BlockModel model = entry.getValue().apply(Objects.requireNonNull(entry.getKey().getId()).mapPath(path -> "blocks/" + path + ".g3dj"));
            QuantumClient.invokeAndWait(() -> model.load(client));
            FINISHED_REGISTRY.put(entry.getKey(), model);
        }
    }

    /**
     * Gets the block entity model.
     *
     * @param type the type of the block entity.
     * @return the block entity model.
     */
    @Nullable
    public static BlockModel get(BlockEntityType<?> type) {
        return FINISHED_REGISTRY.get(type);
    }
}
