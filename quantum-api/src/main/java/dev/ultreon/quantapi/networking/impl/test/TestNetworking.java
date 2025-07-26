package dev.ultreon.quantapi.networking.impl.test;

import dev.ultreon.quantapi.networking.api.INetworkEntryPoint;
import dev.ultreon.quantapi.networking.impl.Network;
import dev.ultreon.xeox.api.IXeoxLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TestNetworking implements INetworkEntryPoint {
    private static Network network;

    public static Network getNetwork() {
        return network;
    }

    @Override
    public void init() {
        if (!IXeoxLoader.get().isDevEnvironment()) return;

        TestNetworking.network = new TestNetwork();
    }
}
