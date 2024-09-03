package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record S2CAddPlayerPacket(UUID uuid, String name, Vec3d position) implements Packet<InGameClientPacketHandler> {
    public S2CAddPlayerPacket(@NotNull UUID uuid, String name, Vec3d position) {
        this.uuid = uuid;
        this.name = name;
        this.position = position;
    }

    public static S2CAddPlayerPacket read(PacketIO buffer) {
        var uuid = buffer.readUuid();
        var name = buffer.readString(20);
        var position = buffer.readVec3d();

        return new S2CAddPlayerPacket(uuid, name, position);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
        buffer.writeUTF(this.name, 20);
        buffer.writeVec3d(this.position);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onAddPlayer(this.uuid, this.name, this.position);
    }

    @Override
    public String toString() {
        return "S2CAddPlayerPacket{" +
               "uuid=" + uuid +
               ", name='" + name + '\'' +
               ", position=" + position +
               '}';
    }
}
