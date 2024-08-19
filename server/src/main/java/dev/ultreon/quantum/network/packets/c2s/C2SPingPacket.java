package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SPingPacket extends Packet<InGameServerPacketHandler> {
    private final long time;

    public C2SPingPacket() {
        this.time = System.currentTimeMillis();
    }

    public C2SPingPacket(PacketIO buffer) {
        this.time = buffer.readLong();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(this.time);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPing(this.time);
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "C2SPingPacket{" +
                "time=" + time +
                '}';
    }
}
