package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.api.PacketDestination;
import dev.ultreon.quantum.network.packets.Packet;

public interface PacketHandler {
    PacketDestination destination();

    void onDisconnect(String message);

    boolean isAcceptingPackets();

    default boolean shouldHandlePacket(Packet<?> packet) {
        return this.isAcceptingPackets();
    }

    PacketContext context();

    default boolean isAsync() {
        return true;
    }

    boolean isDisconnected();

    Packet<?> reply(long sequenceId);
}
