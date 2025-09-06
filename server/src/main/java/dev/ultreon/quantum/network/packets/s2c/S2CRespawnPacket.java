package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.DVec3;

import java.util.Objects;

public final class S2CRespawnPacket implements Packet<InGameClientPacketHandler> {
    private final DVec3 pos;

    public S2CRespawnPacket(DVec3 pos) {
        this.pos = pos;
    }

    public static S2CRespawnPacket read(PacketIO buffer) {
        var pos = buffer.readVec3d(new DVec3());

        return new S2CRespawnPacket(pos);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVec3d(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRespawn(this.pos);
    }

    @Override
    public String toString() {
        return "S2CRespawnPacket{" +
               "pos=" + pos +
               '}';
    }

    public DVec3 pos() {
        return pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CRespawnPacket) obj;
        return Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

}
