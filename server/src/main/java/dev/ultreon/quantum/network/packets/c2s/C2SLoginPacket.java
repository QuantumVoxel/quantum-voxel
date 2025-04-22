package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.LoginServerPacketHandler;

import java.util.Objects;

public final class C2SLoginPacket implements Packet<LoginServerPacketHandler> {
    private final String name;

    public C2SLoginPacket(String name) {
        this.name = name;
    }

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

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SLoginPacket) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "C2SLoginPacket[" +
               "name=" + name + ']';
    }

}
