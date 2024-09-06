package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.server.player.ServerPlayer;
import io.netty.handler.codec.DecoderException;

public record C2SChatPacket(String message) implements Packet<InGameServerPacketHandler> {

    public static C2SChatPacket read(PacketIO buffer) {
        var message = buffer.readString(1024);

        return new C2SChatPacket(message);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUTF(this.message, 1024);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        ServerPlayer player = ctx.getPlayer();
        if (player == null) throw new DecoderException("Player is null!");
        player.onMessageSent(this.message);
    }

    @Override
    public String toString() {
        return "C2SChatPacket{message=" + this.message + '}';
    }
}
