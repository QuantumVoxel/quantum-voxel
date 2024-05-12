package dev.ultreon.xeox.loader;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Registry API for XeoxJS.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class JSRegistries {
    public Registry<?> registry(Identifier id) {
        return Registries.REGISTRY.get(id);
    }
    public Registry<?> registry(String name) {
        return Registries.REGISTRY.get(Identifier.parse(name));
    }
    public @Nullable Identifier id(String name) {
        return Identifier.tryParse(name);
    }

    public Identifier id(String namespace, String path) {
        return new Identifier(namespace, path);
    }

    public Registry.Builder<?> createBuilder(Identifier id) {
        return new Registry.Builder<>(id);
        
    }
}
