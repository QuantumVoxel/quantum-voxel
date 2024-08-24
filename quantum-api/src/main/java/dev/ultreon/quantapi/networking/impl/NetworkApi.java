package dev.ultreon.quantapi.networking.impl;

import dev.ultreon.quantapi.networking.api.INetwork;
import dev.ultreon.quantapi.networking.api.INetworkApi;
import dev.ultreon.quantapi.networking.api.INetworkFactory;
import dev.ultreon.quantapi.networking.api.IPacketRegisterContext;

public class NetworkApi implements INetworkApi {
    @Override
    public INetwork createNetwork(INetworkFactory network, String modId, String channelName) {
        return new Network(modId, channelName) {
            @Override
            protected void registerPackets(IPacketRegisterContext ctx) {
                network.registerPackets(ctx);
            }
        };
    }
}
