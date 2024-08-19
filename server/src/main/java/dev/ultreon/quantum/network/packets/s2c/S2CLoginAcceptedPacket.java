package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.UUID;

public class S2CLoginAcceptedPacket extends Packet<LoginClientPacketHandler> {
    private final UUID uuid;

    public S2CLoginAcceptedPacket(UUID uuid) {
        this.uuid = uuid;
    }

    public S2CLoginAcceptedPacket(PacketIO buffer) {
        this.uuid = buffer.readUuid();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
    }

    @Override
    public void handle(PacketContext ctx, LoginClientPacketHandler handler) {
        handler.onLoginAccepted(this.uuid);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String toString() {
        return "S2CLoginAcceptedPacket{" +
                "uuid=" + uuid +
                '}';
    }
}
