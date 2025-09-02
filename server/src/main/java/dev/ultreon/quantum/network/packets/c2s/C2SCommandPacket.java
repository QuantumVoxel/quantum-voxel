package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.Objects;

public final class C2SCommandPacket implements Packet<InGameServerPacketHandler> {
    private final String input;

    public C2SCommandPacket(String input) {
        this.input = input;
    }

    public static C2SCommandPacket read(PacketIO buffer) {
        var input = buffer.readString(32768);

        return new C2SCommandPacket(input);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeString(this.input, 32768);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        ServerPlayer player = ctx.getPlayer();
        if (player != null) {
            player.runCommand(this.input);
        }
    }

    @Override
    public String toString() {
        return "C2SCommandPacket{" +
               "input='" + input + '\'' +
               '}';
    }

    public String input() {
        return input;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SCommandPacket) obj;
        return Objects.equals(this.input, that.input);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input);
    }

}
