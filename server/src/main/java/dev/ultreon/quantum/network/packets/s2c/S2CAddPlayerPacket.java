package dev.ultreon.quantum.network.packets.s2c;

import com.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class S2CAddPlayerPacket extends Packet<InGameClientPacketHandler> {
    private final UUID uuid;
    private final String name;
    private final Vec3d position;

    public S2CAddPlayerPacket(@NotNull UUID uuid, String name, Vec3d position) {
        this.uuid = uuid;
        this.name = name;
        this.position = position;
    }

    public S2CAddPlayerPacket(PacketIO buffer) {
        this.uuid = buffer.readUuid();
        this.name = buffer.readString(20);
        this.position = buffer.readVec3d();
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
}
