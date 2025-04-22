package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class S2CRemovePlayerPacket implements Packet<InGameClientPacketHandler> {
    private final UUID uuid;

    public S2CRemovePlayerPacket(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public static S2CRemovePlayerPacket read(PacketIO buffer) {
        var uuid = buffer.readUuid();

        return new S2CRemovePlayerPacket(uuid);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemovePlayer(this.uuid);
    }

    @Override
    public String toString() {
        return "S2CRemovePlayerPacket{" +
               "uuid=" + uuid +
               '}';
    }

    public UUID uuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CRemovePlayerPacket) obj;
        return Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
