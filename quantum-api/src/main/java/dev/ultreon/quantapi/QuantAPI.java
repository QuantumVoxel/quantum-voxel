package dev.ultreon.quantapi;

import dev.ultreon.quantapi.networking.api.INetworkEntryPoint;
import dev.ultreon.quantapi.networking.impl.Network;
import dev.ultreon.quantapi.networking.impl.NetworkManager;
import dev.ultreon.quantapi.networking.impl.test.TestNetworking;
import dev.ultreon.quantapi.networking.impl.test.TestPacket;
import dev.ultreon.quantum.ModInitializer;
import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.api.events.ServerPlayerEvent;
import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;
import dev.ultreon.xeox.api.IXeoxLoader;

@SuppressWarnings("unused")
public class QuantAPI implements ModInitializer {
    public static final String MOD_ID = "quantapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(QuantAPI.class);

    @Override
    public void onInitialize() {
        // Load the network services using Java's service loader.
        IXeoxLoader.get().invokeEntrypoints("quantapi-networking", INetworkEntryPoint.class, INetworkEntryPoint::init);

        NetworkManager.init();

        if (IXeoxLoader.get().isDevEnvironment()) {
            EventSystem.addListenerDefault(ServerPlayerEvent.Join.class, event -> {
                Network network = TestNetworking.getNetwork();
                network.sendPlayer(new TestPacket(), event.getEntity());
            });
        }
    }
}
