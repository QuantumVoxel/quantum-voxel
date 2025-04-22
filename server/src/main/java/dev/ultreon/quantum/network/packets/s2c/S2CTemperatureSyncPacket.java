package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.Objects;

public final class S2CTemperatureSyncPacket implements Packet<InGameClientPacketHandler> {
    private final double temperature;

    public S2CTemperatureSyncPacket(double temperature) {
        this.temperature = temperature;
    }

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

    public double temperature() {
        return temperature;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CTemperatureSyncPacket) obj;
        return Double.doubleToLongBits(this.temperature) == Double.doubleToLongBits(that.temperature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature);
    }

    @Override
    public String toString() {
        return "S2CTemperatureSyncPacket[" +
               "temperature=" + temperature + ']';
    }

}
