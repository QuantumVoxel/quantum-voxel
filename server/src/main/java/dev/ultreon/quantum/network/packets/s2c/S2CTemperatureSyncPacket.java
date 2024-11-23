package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CTemperatureSyncPacket(double temperature) implements Packet<InGameClientPacketHandler> {
    public static S2CTemperatureSyncPacket read(PacketIO buffer) {
        return new S2CTemperatureSyncPacket(buffer.readDouble());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeDouble(temperature);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onTemperatureSync(this);
    }
}
