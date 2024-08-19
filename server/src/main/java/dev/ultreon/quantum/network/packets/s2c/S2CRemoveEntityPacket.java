package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CRemoveEntityPacket extends Packet<InGameClientPacketHandler> {
    private final int id;;

    public S2CRemoveEntityPacket(int id) {
        this.id = id;
    }

    public S2CRemoveEntityPacket(PacketIO buffer) {
        this.id = buffer.readVarInt();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(this.id);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemoveEntity(this.id);
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "S2CRemoveEntityPacket(id=" + this.id + ")";
    }
}
