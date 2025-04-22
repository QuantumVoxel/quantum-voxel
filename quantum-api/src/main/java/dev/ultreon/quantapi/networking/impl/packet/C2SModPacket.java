package dev.ultreon.quantapi.networking.impl.packet;

import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantapi.networking.impl.ModNetChannel;
import dev.ultreon.quantapi.networking.impl.ModPacketContext;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;

public final class C2SModPacket implements dev.ultreon.quantum.network.packets.Packet<InGameServerPacketHandler> {
    private final ModNetChannel channel;
    private final NamespaceID channelId;
    private final Packet<?> packet;

    public C2SModPacket(ModNetChannel channel, NamespaceID channelId,
                        Packet<?> packet) {
        this.channel = channel;
        this.channelId = channelId;
        this.packet = packet;
    }

    public C2SModPacket(ModNetChannel channel, Packet<?> packet) {
        this(channel, channel.id(), packet);
    }

    public static C2SModPacket read(PacketIO buffer) {
        var channelId = buffer.readId();
        var channel = ModNetChannel.getChannel(channelId);
        var packet = channel.getDecoder(buffer.readUnsignedShort()).apply(buffer);

        return new C2SModPacket(channel, channelId, packet);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(this.channelId);
        buffer.writeShort(this.channel.getId(this.packet));

        this.packet.toBytes(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        this.packet.handlePacket(() -> new ModPacketContext(this.channel, handler.context().getPlayer(), handler.context().getConnection(), Env.SERVER));
    }

    public ModNetChannel channel() {
        return channel;
    }

    public NamespaceID channelId() {
        return channelId;
    }

    public Packet<?> packet() {
        return packet;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SModPacket) obj;
        return Objects.equals(this.channel, that.channel) &&
               Objects.equals(this.channelId, that.channelId) &&
               Objects.equals(this.packet, that.packet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, channelId, packet);
    }

    @Override
    public String toString() {
        return "C2SModPacket[" +
               "channel=" + channel + ", " +
               "channelId=" + channelId + ", " +
               "packet=" + packet + ']';
    }

}
