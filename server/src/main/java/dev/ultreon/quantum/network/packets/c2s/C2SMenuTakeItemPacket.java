package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SMenuTakeItemPacket(int index, boolean rightClick) implements Packet<InGameServerPacketHandler> {

    public static C2SMenuTakeItemPacket read(PacketIO buffer) {
        var index = buffer.readInt();
        var rightClick = buffer.readBoolean();

        return new C2SMenuTakeItemPacket(index, rightClick);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.rightClick);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onTakeItem(this.index, this.rightClick);
    }
}
