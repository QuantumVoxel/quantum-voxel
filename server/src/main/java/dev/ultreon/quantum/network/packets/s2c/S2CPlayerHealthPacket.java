package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public record S2CPlayerHealthPacket(float newHealth) implements Packet<InGameClientPacketHandler> {

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
}
