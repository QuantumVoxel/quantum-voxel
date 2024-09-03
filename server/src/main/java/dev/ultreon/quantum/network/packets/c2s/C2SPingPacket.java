package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SPingPacket(long time) implements Packet<InGameServerPacketHandler> {

    public C2SPingPacket() {
        this(System.currentTimeMillis());
    }

    public static C2SPingPacket read(PacketIO buffer) {
        var time = buffer.readLong();

        return new C2SPingPacket(time);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(this.time);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPing(this.time);
    }

    @Override
    public String toString() {
        return "C2SPingPacket{" +
               "time=" + time +
               '}';
    }
}
