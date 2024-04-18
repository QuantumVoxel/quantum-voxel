package com.ultreon.quantum.client.registry;

import com.ultreon.quantum.client.InternalApi;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class LanguageRegistry {
    private static final Set<Identifier> REGISTRY = new HashSet<>();

    @InternalApi
    @ApiStatus.Internal
    public static void doRegistration(Consumer<Identifier> consumer) {
        LanguageRegistry.REGISTRY.forEach(consumer);
    }

    public static void register(Identifier id) {
        LanguageRegistry.REGISTRY.add(id);
    }
}
