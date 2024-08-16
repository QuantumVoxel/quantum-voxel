package dev.ultreon.xeox.loader;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

/**
 * Registry API for XeoxJS.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class JSRegistries {
    public Registry<?> registry(NamespaceID id) {
        return Registries.REGISTRY.get(id);
    }
    public Registry<?> registry(String name) {
        return Registries.REGISTRY.get(NamespaceID.parse(name));
    }
    public @Nullable NamespaceID id(String name) {
        return NamespaceID.tryParse(name);
    }

    public NamespaceID id(String namespace, String path) {
        return new NamespaceID(namespace, path);
    }

    public Registry.Builder<?> createBuilder(NamespaceID id) {
        return new Registry.Builder<>(id);
        
    }
}
