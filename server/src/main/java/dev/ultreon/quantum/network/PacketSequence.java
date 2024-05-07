package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.packets.Packet;

public record PacketSequence<T extends PacketHandler>(long sequence, Packet<T> packet) {
}
