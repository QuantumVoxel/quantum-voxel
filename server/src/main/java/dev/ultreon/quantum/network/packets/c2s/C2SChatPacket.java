package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.DecoderException;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.Objects;

public final class C2SChatPacket implements Packet<InGameServerPacketHandler> {
    private final String message;

    public C2SChatPacket(String message) {
        this.message = message;
    }

    public static C2SChatPacket read(PacketIO buffer) {
        var message = buffer.readString(1024);

        return new C2SChatPacket(message);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeString(this.message, 1024);
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

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SChatPacket) obj;
        return Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

}
