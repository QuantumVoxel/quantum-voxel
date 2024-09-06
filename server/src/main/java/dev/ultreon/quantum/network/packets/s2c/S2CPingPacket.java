package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CPingPacket(long serverTime, long time) implements Packet<ClientPacketHandler> {

    public S2CPingPacket(long time) {
        this(System.currentTimeMillis(), time);
    }

    public static S2CPingPacket read(PacketIO buffer) {
        var serverTime = buffer.readLong();
        var time = buffer.readLong();

        return new S2CPingPacket(serverTime, time);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(this.serverTime);
        buffer.writeLong(this.time);
    }

    @Override
    public void handle(PacketContext ctx, ClientPacketHandler handler) {
        if (handler instanceof InGameClientPacketHandler inGameHandler) {
            inGameHandler.onPing(this.serverTime, this.time);
        }
    }

    @Override
    public String toString() {
        return "S2CPingPacket{serverTime=" + this.serverTime + ", time=" + this.time + '}';
    }
}
