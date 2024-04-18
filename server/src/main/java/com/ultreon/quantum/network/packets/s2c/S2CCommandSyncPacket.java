package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;

import java.util.List;

public class S2CCommandSyncPacket extends Packet<InGameClientPacketHandler> {
    private final List<String> commands;

    public S2CCommandSyncPacket(List<String> commands) {
        this.commands = commands;
    }

    public S2CCommandSyncPacket(PacketIO buffer) {
        this.commands = buffer.readList(buf -> buf.readString(64));
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeList(this.commands, (buf, s) -> buf.writeUTF(s, 64));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {

    }
}
