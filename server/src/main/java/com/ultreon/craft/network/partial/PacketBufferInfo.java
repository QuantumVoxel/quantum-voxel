package com.ultreon.craft.network.partial;

import com.ultreon.craft.network.PacketBuffer;

public record PacketBufferInfo(int packetId, long sequence, PacketBuffer buffer) {

}
