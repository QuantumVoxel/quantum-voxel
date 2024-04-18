package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class S2CTabCompletePacket extends Packet<InGameClientPacketHandler> {
    private final List<String> options;

    public S2CTabCompletePacket(@Nullable List<String> options) {
        this.options = options;
    }

    public S2CTabCompletePacket(PacketIO buffer) {
        this.options = buffer.readList(buf -> buf.readString(64));
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeList(this.options, (buf, elem) -> buf.writeUTF(elem, 64));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onTabCompleteResult(this.options.toArray(new String[0]));
    }
}
