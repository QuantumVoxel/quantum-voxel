package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class S2CTabCompletePacket implements Packet<InGameClientPacketHandler> {
    private final @Nullable List<String> options;

    public S2CTabCompletePacket(@Nullable List<String> options) {
        this.options = options;
    }

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

    public @Nullable List<String> options() {
        return options;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CTabCompletePacket) obj;
        return Objects.equals(this.options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options);
    }

}
