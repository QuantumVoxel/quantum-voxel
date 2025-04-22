package dev.ultreon.quantapi;

import dev.ultreon.quantapi.networking.api.INetworkEntryPoint;
import dev.ultreon.quantapi.networking.impl.Network;
import dev.ultreon.quantapi.networking.impl.NetworkManager;
import dev.ultreon.quantapi.networking.impl.test.TestNetworkEntry;
import dev.ultreon.quantapi.networking.impl.test.TestPacket;
import dev.ultreon.quantum.events.PlayerEvents;
import net.fabricmc.loader.api.FabricLoader;
import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;

import java.util.ServiceLoader;

@SuppressWarnings("unused")
public class QuantAPI implements ModInit {
    public static final String MOD_ID = "uc_networking";
    public static final Logger LOGGER = LoggerFactory.getLogger(QuantAPI.class);

    @Override
    public void onInitialize() {
        // Load the network services using Java's service loader.
        FabricLoader.getInstance().invokeEntrypoints("uc_networking:init", INetworkEntryPoint.class, INetworkEntryPoint::init);

        for (INetworkEntryPoint networkInit : ServiceLoader.load(INetworkEntryPoint.class)) {
            networkInit.init();
        }

        NetworkManager.init();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            PlayerEvents.PLAYER_JOINED.subscribe(player -> {
                Network network = TestNetworkEntry.getNetwork();
                network.sendPlayer(new TestPacket(), player);
            });
        }
    }
}
