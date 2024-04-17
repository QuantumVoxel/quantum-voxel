package com.ultreon.craft.network.partial;

import com.ultreon.craft.network.PacketIO;

public record PacketBufferInfo(int packetId, long sequence, PacketIO buffer) {

}
