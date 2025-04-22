package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.Objects;

public final class S2CTimeSyncPacket implements Packet<InGameClientPacketHandler> {
    private final long gameTime;

    public S2CTimeSyncPacket(long gameTime) {
        this.gameTime = gameTime;
    }

    public static S2CTimeSyncPacket read(PacketIO buffer) {
        long gameTime = buffer.readLong();
        return new S2CTimeSyncPacket(gameTime);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(gameTime);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.handleTimeSync(this, ctx);
    }

    public long gameTime() {
        return gameTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CTimeSyncPacket) obj;
        return this.gameTime == that.gameTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameTime);
    }

    @Override
    public String toString() {
        return "S2CTimeSyncPacket[" +
               "gameTime=" + gameTime + ']';
    }

}
