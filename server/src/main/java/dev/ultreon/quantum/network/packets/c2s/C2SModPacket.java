package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.api.packet.ServerEndpoint;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.NamespaceID;

public record C2SModPacket(NetworkChannel channel, NamespaceID channelId,
                           ModPacket<?> packet) implements Packet<InGameServerPacketHandler> {


    public <T extends ModPacket<T> & ServerEndpoint> C2SModPacket(NetworkChannel channel, ModPacket<T> modPacket) {
        this(channel, channel.id(), modPacket);
    }

    public static C2SModPacket read(PacketIO buffer) {
        var channelId = buffer.readId();
        var channel = NetworkChannel.getChannel(channelId);
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
        handler.onModPacket(this.channel, this.packet);
    }

    @Override
    public String toString() {
        return "C2SModPacket{" +
               "channelId=" + channelId +
               ", packet=" + packet +
               '}';
    }
}
