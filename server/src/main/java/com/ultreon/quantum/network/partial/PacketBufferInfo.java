package com.ultreon.quantum.network.partial;

import com.ultreon.quantum.network.PacketIO;

public record PacketBufferInfo(int packetId, long sequence, PacketIO buffer) {

}
