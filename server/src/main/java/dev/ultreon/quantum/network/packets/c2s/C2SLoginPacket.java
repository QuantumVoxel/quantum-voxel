package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.LoginServerPacketHandler;

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
