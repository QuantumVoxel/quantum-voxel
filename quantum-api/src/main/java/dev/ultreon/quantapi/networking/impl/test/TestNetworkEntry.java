package dev.ultreon.quantapi.networking.impl.test;

import dev.ultreon.quantapi.networking.api.INetworkEntryPoint;
import dev.ultreon.quantapi.networking.impl.Network;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TestNetworkEntry implements INetworkEntryPoint {
    private static Network network;

    public static Network getNetwork() {
        return network;
    }

    @Override
    public void init() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        TestNetworkEntry.network = new TestNetwork();
    }
}
