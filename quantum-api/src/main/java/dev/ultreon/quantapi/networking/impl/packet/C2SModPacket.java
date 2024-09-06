package dev.ultreon.quantapi.networking.impl.packet;

import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantapi.networking.impl.ModNetChannel;
import dev.ultreon.quantapi.networking.impl.ModPacketContext;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.NamespaceID;

public record C2SModPacket(ModNetChannel channel, NamespaceID channelId,
                           Packet<?> packet) implements dev.ultreon.quantum.network.packets.Packet<InGameServerPacketHandler> {

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
}
