package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.client.InternalApi;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class LanguageRegistry {
    private static final Set<NamespaceID> REGISTRY = new HashSet<>();

    @InternalApi
    @ApiStatus.Internal
    public static void doRegistration(Consumer<NamespaceID> consumer) {
        LanguageRegistry.REGISTRY.forEach(consumer);
    }

    public static void register(NamespaceID id) {
        LanguageRegistry.REGISTRY.add(id);
    }
}
