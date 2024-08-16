package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.NetworkChannel;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;

public class S2CModPacket extends Packet<InGameClientPacketHandler> {
    private final NamespaceID channelId;
    private final ModPacket<?> packet;
    private final NetworkChannel channel;

    public S2CModPacket(NetworkChannel channel, ModPacket<?> packet) {
        this.channel = channel;
        this.channelId = channel.id();
        this.packet = packet;
    }

    public S2CModPacket(PacketIO buffer) {
        this.channelId = buffer.readId();
        this.channel = NetworkChannel.getChannel(this.channelId);
        this.packet = this.channel.getDecoder(buffer.readUnsignedShort()).apply(buffer);
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
}
