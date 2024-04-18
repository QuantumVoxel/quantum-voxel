package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.BlockPos;
import org.jetbrains.annotations.Nullable;

public class C2SOpenMenuPacket extends Packet<InGameServerPacketHandler> {
    private final Identifier id;
    private final BlockPos pos;

    public C2SOpenMenuPacket(PacketIO buffer) {
        this.id = buffer.readId();
        this.pos = buffer.readBoolean() ? buffer.readBlockPos() : null;
    }

    public C2SOpenMenuPacket(Identifier id, @Nullable BlockPos pos) {
        this.id = id;
        this.pos = pos;
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(id);
        buffer.writeBoolean(pos != null);
        if (pos != null) {
            buffer.writeBlockPos(pos);
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.handleOpenMenu(id, pos);
    }
}
