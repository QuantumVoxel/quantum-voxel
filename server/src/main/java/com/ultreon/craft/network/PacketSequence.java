package com.ultreon.craft.network;

import com.ultreon.craft.network.packets.Packet;

public record PacketSequence<T extends PacketHandler>(long sequence, Packet<T> packet) {
}
