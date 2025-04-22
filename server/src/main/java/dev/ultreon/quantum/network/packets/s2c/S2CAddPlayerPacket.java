package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class S2CAddPlayerPacket implements Packet<InGameClientPacketHandler> {
    private final int id;
    private final UUID uuid;
    private final String name;
    private final Vec3d position;

    public S2CAddPlayerPacket(int id, @NotNull UUID uuid, String name, Vec3d position) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.position = position;
    }

    public static S2CAddPlayerPacket read(PacketIO buffer) {
        var id = buffer.readInt();
        var uuid = buffer.readUuid();
        var name = buffer.readString(20);
        var position = buffer.readVec3d();

        return new S2CAddPlayerPacket(id, uuid, name, position);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.id);
        buffer.writeUuid(this.uuid);
        buffer.writeString(this.name, 20);
        buffer.writeVec3d(this.position);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onAddPlayer(id, this.uuid, this.name, this.position);
    }

    @Override
    public String toString() {
        return "S2CAddPlayerPacket{" +
               "uuid=" + uuid +
               ", name='" + name + '\'' +
               ", position=" + position +
               '}';
    }

    public int id() {
        return id;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public Vec3d position() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CAddPlayerPacket) obj;
        return this.id == that.id &&
               Objects.equals(this.uuid, that.uuid) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, name, position);
    }

}
