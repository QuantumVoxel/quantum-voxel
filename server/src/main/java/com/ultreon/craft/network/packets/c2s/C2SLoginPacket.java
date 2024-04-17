package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketIO;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.LoginServerPacketHandler;

public class C2SLoginPacket extends Packet<LoginServerPacketHandler> {
    private final String name;

    public C2SLoginPacket(String name) {
        this.name = name;
    }

    public C2SLoginPacket(PacketIO buffer) {
        this.name = buffer.readString(20);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUTF(this.name, 20);
    }

    @Override
    public void handle(PacketContext ctx, LoginServerPacketHandler handler) {
        handler.onPlayerLogin(this.name);
    }
}
