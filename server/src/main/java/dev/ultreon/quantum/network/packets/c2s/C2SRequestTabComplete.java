package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.server.player.ServerPlayer;

public record C2SRequestTabComplete(String input) implements Packet<InGameServerPacketHandler> {

    public static C2SRequestTabComplete read(PacketIO buffer) {
        var input = buffer.readString(32768);

        return new C2SRequestTabComplete(input);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUTF(this.input, 32768);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        ServerPlayer player = ctx.getPlayer();
        if (player != null) {
            player.tabComplete(this.input);
        }
    }

    @Override
    public String toString() {
        return "C2SRequestTabComplete{" +
               "input='" + input + '\'' +
               '}';
    }
}
