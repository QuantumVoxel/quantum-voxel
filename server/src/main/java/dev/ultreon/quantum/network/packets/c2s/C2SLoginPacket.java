package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.LoginServerPacketHandler;

public record C2SLoginPacket(String name) implements Packet<LoginServerPacketHandler> {

    public static C2SLoginPacket read(PacketIO buffer) {
        var name = buffer.readString(20);

        return new C2SLoginPacket(name);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeString(this.name, 20);
    }

    @Override
    public void handle(PacketContext ctx, LoginServerPacketHandler handler) {
        handler.onPlayerLogin(this.name);
    }
}
