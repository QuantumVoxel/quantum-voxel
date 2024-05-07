package dev.ultreon.quantum.network.partial;

import dev.ultreon.quantum.network.PacketIO;

public record PacketBufferInfo(int packetId, long sequence, PacketIO buffer) {

}
