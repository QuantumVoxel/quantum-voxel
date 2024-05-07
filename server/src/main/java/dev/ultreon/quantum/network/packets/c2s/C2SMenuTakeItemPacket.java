package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SMenuTakeItemPacket extends Packet<InGameServerPacketHandler> {
    private final int index;
    private final boolean rightClick;

    public C2SMenuTakeItemPacket(int index, boolean rightClick) {
        this.index = index;
        this.rightClick = rightClick;
    }

    public C2SMenuTakeItemPacket(PacketIO buffer) {
        this.index = buffer.readInt();
        this.rightClick = buffer.readBoolean();
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
