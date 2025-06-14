package dev.ultreon.quantum.network.api;

import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashMap;
import java.util.Map;

public class NetworkManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    private static final Map<NamespaceID, Network> NETWORKS = new HashMap<>();

    public static void registerNetwork(Network network) {
        if (NetworkManager.NETWORKS.containsKey(network.getId())) {
            NetworkManager.LOGGER.error("Network with id {} already registered, don't manually register the network class.", network.getId());
            return;
        }

        NetworkManager.NETWORKS.put(network.getId(), network);
    }

    public static void init() {
        NetworkManager.NETWORKS.values().forEach(Network::init);
    }
}
