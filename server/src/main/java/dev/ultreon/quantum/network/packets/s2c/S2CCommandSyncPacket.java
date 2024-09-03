package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.List;

public record S2CCommandSyncPacket(List<String> commands) implements Packet<InGameClientPacketHandler> {

    public static S2CCommandSyncPacket read(PacketIO buffer) {
        var commands = buffer.readList(buf -> buf.readString(64));

        return new S2CCommandSyncPacket(commands);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeList(this.commands, (buf, s) -> buf.writeUTF(s, 64));
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
}
