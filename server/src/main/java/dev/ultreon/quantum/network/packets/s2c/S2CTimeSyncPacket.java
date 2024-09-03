package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CTimeSyncPacket(long gameTime) implements Packet<InGameClientPacketHandler> {
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
}
