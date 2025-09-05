package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.network.PacketHandler;

import java.util.List;

public interface BundlePacket<TheirHandler extends PacketHandler> extends Packet<TheirHandler> {

    List<Packet<TheirHandler>> getPackets();
}
