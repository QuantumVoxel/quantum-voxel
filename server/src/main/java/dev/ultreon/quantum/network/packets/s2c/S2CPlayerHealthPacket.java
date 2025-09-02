package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.Objects;

public final class S2CPlayerHealthPacket implements Packet<InGameClientPacketHandler> {
    private final float newHealth;

    public S2CPlayerHealthPacket(float newHealth) {
        this.newHealth = newHealth;
    }

    public static S2CPlayerHealthPacket read(PacketIO buffer) {
        var newHealth = buffer.readFloat();

        return new S2CPlayerHealthPacket(newHealth);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeFloat(this.newHealth);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerHealth(this.newHealth);
    }

    @Override
    public String toString() {
        return "S2CPlayerHealthPacket{" +
               "newHealth=" + newHealth +
               '}';
    }

    public float newHealth() {
        return newHealth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CPlayerHealthPacket) obj;
        return Float.floatToIntBits(this.newHealth) == Float.floatToIntBits(that.newHealth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newHealth);
    }

}
