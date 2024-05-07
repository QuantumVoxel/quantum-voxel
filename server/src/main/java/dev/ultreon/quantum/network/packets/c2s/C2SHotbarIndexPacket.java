package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public class C2SHotbarIndexPacket extends Packet<InGameServerPacketHandler> {
    private final int hotbarIdx;

    public C2SHotbarIndexPacket(int hotbarIdx) {
        this.hotbarIdx = hotbarIdx;
    }

    public C2SHotbarIndexPacket(PacketIO buffer) {
        this.hotbarIdx = buffer.readByte();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(this.hotbarIdx);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onHotbarIndex(this.hotbarIdx);
    }
}
