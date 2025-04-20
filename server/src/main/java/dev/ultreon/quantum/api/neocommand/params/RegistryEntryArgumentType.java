package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.neocommand.*;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.List;

public class RegistryEntryArgumentType<T> implements ArgumentType<T> {
    private final RegistryKey<Registry<T>> key;

    public RegistryEntryArgumentType(RegistryKey<Registry<T>> key) {
        this.key = key;
    }

    @Override
    public T parse(CommandReader ctx) throws CommandParseException {
        NamespaceID namespaceID = ctx.nextID();
        QuantumServer server = ctx.getServer();
        return server.getRegistries().getOrGeneric(key).get(namespaceID);
    }

    @Override
    public void complete(SuggestionProvider ctx) {
        Registry<T> registry = ctx.getServer().getRegistries().getOrGeneric(key);
        for (RegistryKey<T> value : registry.keys()) {
            ctx.suggest(value.id());
        }
    }

    @Override
    public boolean matches(CommandReader arg) {
        return NamespaceID.tryParse(arg.nextWord()) != null;
    }

    @Override
    public List<String> getExamples() {
        return List.of(
                "quantum:stone",
                "quantum:blocks/stone",
                "stone",
                "blocks/stone"
        );
    }

    public static <T> Parameter<T> registryEntry(String name, RegistryKey<Registry<T>> key) { return new Parameter<>(name, new RegistryEntryArgumentType<>(key)); }
}
