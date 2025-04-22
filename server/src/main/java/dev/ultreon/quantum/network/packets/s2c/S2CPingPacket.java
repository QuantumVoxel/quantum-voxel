package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.Objects;

public final class S2CPingPacket implements Packet<ClientPacketHandler> {
    private final long serverTime;
    private final long time;

    public S2CPingPacket(long serverTime, long time) {
        this.serverTime = serverTime;
        this.time = time;
    }

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
        if (handler instanceof InGameClientPacketHandler) {
            InGameClientPacketHandler inGameHandler = (InGameClientPacketHandler) handler;
            inGameHandler.onPing(this.serverTime, this.time);
        }
    }

    @Override
    public String toString() {
        return "S2CPingPacket{serverTime=" + this.serverTime + ", time=" + this.time + '}';
    }

    public long serverTime() {
        return serverTime;
    }

    public long time() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CPingPacket) obj;
        return this.serverTime == that.serverTime &&
               this.time == that.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverTime, time);
    }

}
