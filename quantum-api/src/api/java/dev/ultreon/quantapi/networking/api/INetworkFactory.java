package dev.ultreon.quantapi.networking.api;

@FunctionalInterface
public interface INetworkFactory {
    void registerPackets(IPacketRegisterContext ctx);
}
