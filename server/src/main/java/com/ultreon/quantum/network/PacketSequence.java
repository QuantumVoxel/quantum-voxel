package com.ultreon.quantum.network;

import com.ultreon.quantum.network.packets.Packet;

public record PacketSequence<T extends PacketHandler>(long sequence, Packet<T> packet) {
}
