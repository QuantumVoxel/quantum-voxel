package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.network.PacketHandler;

public interface BundlePacket<TheirHandler extends PacketHandler> extends Packet<TheirHandler> {

}
