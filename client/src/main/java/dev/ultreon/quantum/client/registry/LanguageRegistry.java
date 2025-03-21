package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Represents the registry of languages.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class LanguageRegistry {
    private static final Set<NamespaceID> REGISTRY = new HashSet<>();

    /**
     * Registers a language.
     *
     * @param consumer the consumer.
     */
    @ApiStatus.Internal
    public static void doRegistration(Consumer<NamespaceID> consumer) {
        LanguageRegistry.REGISTRY.forEach(consumer);
    }

    /**
     * Registers a language.
     *
     * @param id the id of the language.
     */
    public static void register(NamespaceID id) {
        LanguageRegistry.REGISTRY.add(id);
    }
}
