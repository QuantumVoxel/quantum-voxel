package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;

public record S2CModPacket(NetworkChannel channel, NamespaceID channelId,
                           ModPacket<?> packet) implements Packet<InGameClientPacketHandler> {

    public S2CModPacket(NetworkChannel channel, ModPacket<?> packet) {
        this(channel, channel.id(), packet);
    }

    public static S2CModPacket read(PacketIO buffer) {
        var channelId = buffer.readId();
        var channel = NetworkChannel.getChannel(channelId);
        var packet = channel.getDecoder(buffer.readUnsignedShort()).apply(buffer);

        return new S2CModPacket(channel, channelId, packet);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(this.channelId);
        buffer.writeShort(this.channel.getId(this.packet));

        this.packet.toBytes(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onModPacket(this.channel, this.packet);
    }

    @Override
    public String toString() {
        return "S2CModPacket{" +
               "channelId=" + channelId +
               ", packet=" + packet +
               '}';
    }
}
