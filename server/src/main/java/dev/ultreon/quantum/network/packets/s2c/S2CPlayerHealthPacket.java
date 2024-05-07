package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CPlayerHealthPacket extends Packet<InGameClientPacketHandler> {
    private final float newHealth;

    public S2CPlayerHealthPacket(float newHealth) {
        this.newHealth = newHealth;
    }

    public S2CPlayerHealthPacket(PacketIO buffer) {
        this.newHealth = buffer.readFloat();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeFloat(this.newHealth);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerHealth(this.newHealth);
    }
}
