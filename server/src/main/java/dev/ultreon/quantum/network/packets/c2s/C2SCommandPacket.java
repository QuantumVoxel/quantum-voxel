package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.server.player.ServerPlayer;

public class C2SCommandPacket extends Packet<InGameServerPacketHandler> {
    private final String input;

    public C2SCommandPacket(String input) {
        this.input = input;
    }

    public C2SCommandPacket(PacketIO buffer) {
        this.input = buffer.readString(32768);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUTF(this.input, 32768);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        ServerPlayer player = ctx.getPlayer();
        if (player != null) {
            player.execute(this.input);
        }
    }
}
