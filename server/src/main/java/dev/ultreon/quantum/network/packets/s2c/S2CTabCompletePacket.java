package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record S2CTabCompletePacket(@Nullable List<String> options) implements Packet<InGameClientPacketHandler> {
    public static S2CTabCompletePacket read(PacketIO buffer) {
        var options = buffer.readList(buf -> buf.readString(64));

        return new S2CTabCompletePacket(options);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        if (this.options != null) {
            buffer.writeList(this.options, (buf, elem) -> buf.writeString(elem, 64));
        } else {
            buffer.writeList(List.<String>of(), (buf, elem) -> buf.writeString(elem, 64));
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onTabCompleteResult(this.options.toArray(new String[0]));
    }

    @Override
    public String toString() {
        return "S2CTabCompletePacket{" +
               "options=" + options +
               '}';
    }
}
