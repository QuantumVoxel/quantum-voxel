package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;

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
