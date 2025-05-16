package dev.ultreon.quantum.client.registry;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.MissingRegistriesScreen;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.server.CloseCodes;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashSet;
import java.util.Set;

public class ClientSyncRegistries implements RegistryHandle {
    private final LocalRegistries registries = new LocalRegistries();
    private final QuantumClient client;

    public ClientSyncRegistries(QuantumClient client) {
        this.client = client;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> SyncedRegistry<T> get(RegistryKey<? extends Registry<T>> registryKey) {
        return registries.get((RegistryKey) registryKey);
    }

    public <T> void set(RegistryKey<SyncedRegistry<T>> registryKey, SyncedRegistry<T> registry) {
        registries.set(registryKey, registry);
    }

    public void load(NamespaceID registryID, IntMap<NamespaceID> registryMap) {
        registries.get(registryID).load(registryMap);
    }

    public void load(Array<NamespaceID> registries) {
        Set<NamespaceID> set = this.registries.ids();
        Set<NamespaceID> notFound = new HashSet<>();
        for (NamespaceID entry : registries.items) {
            if (!set.contains(entry)) {
                notFound.add(entry);
            }

            set.remove(entry);
        }

        if (!notFound.isEmpty()) {
            if (!set.isEmpty()) {
                client.connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Missing registries from both sides: " + notFound + " and " + set);
                client.showScreen(new MissingRegistriesScreen(notFound, set));
                return;
            }

            client.connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Missing registries from client: " + notFound);
            client.showScreen(new MissingRegistriesScreen(notFound, Set.of()));
            return;
        }

        if (!set.isEmpty()) {
            client.connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Missing registries from server: " + set);
            client.showScreen(new MissingRegistriesScreen(Set.of(), set));
        }
    }
}
