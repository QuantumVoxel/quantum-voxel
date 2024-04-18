package com.ultreon.quantum.network;

import com.ultreon.quantum.network.api.PacketDestination;
import com.ultreon.quantum.network.packets.Packet;

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
