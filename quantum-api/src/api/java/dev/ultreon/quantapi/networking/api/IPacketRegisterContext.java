package dev.ultreon.quantapi.networking.api;

import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantum.network.PacketIO;

import java.util.function.Function;

public interface IPacketRegisterContext {

    @SuppressWarnings("unchecked")
    <T extends Packet<T>> int register(Function<PacketIO, T> construct, T... type);
}
