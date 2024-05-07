package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.BlockPos;
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
