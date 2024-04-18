package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;
import com.ultreon.quantum.server.player.ServerPlayer;
import io.netty.handler.codec.DecoderException;

public class C2SChatPacket extends Packet<InGameServerPacketHandler> {
    private final String message;

    public C2SChatPacket(String message) {
        this.message = message;
    }

    public C2SChatPacket(PacketIO buffer) {
        this.message = buffer.readString(1024);
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
}
