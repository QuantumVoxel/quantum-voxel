package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CPingPacket extends Packet<ClientPacketHandler> {
    private final long serverTime;
    private final long time;

    public S2CPingPacket(long time) {
        this.serverTime = System.currentTimeMillis();
        this.time = time;
    }

    public S2CPingPacket(PacketIO buffer) {
        this.serverTime = buffer.readLong();
        this.time = buffer.readLong();
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

    public long getServerTime() {
        return this.serverTime;
    }

    public long getTime() {
        return this.time;
    }

    @Override
    public String toString() {
        return "S2CPingPacket{serverTime=" + this.serverTime + ", time=" + this.time + '}';
    }
}
