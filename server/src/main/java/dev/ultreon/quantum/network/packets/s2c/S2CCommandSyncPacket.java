package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.List;
import java.util.Objects;

public final class S2CCommandSyncPacket implements Packet<InGameClientPacketHandler> {
    private final List<String> commands;

    public S2CCommandSyncPacket(List<String> commands) {
        this.commands = commands;
    }

    public static S2CCommandSyncPacket read(PacketIO buffer) {
        var commands = buffer.readList(buf -> buf.readString(64));

        return new S2CCommandSyncPacket(commands);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeList(this.commands, (buf, s) -> buf.writeString(s, 64));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {

    }

    @Override
    public String toString() {
        return "S2CCommandSyncPacket{" +
               "commands=" + commands +
               '}';
    }

    public List<String> commands() {
        return commands;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CCommandSyncPacket) obj;
        return Objects.equals(this.commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commands);
    }

}
