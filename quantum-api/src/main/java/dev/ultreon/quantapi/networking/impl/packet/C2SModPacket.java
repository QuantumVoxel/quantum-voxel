package dev.ultreon.quantapi.networking.impl.packet;

import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantapi.networking.impl.ModPacketContext;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.NamespaceID;

public class C2SModPacket extends dev.ultreon.quantum.network.packets.Packet<InGameServerPacketHandler> {
    private final NamespaceID channelId;
    private final Packet<?> packet;
    private final ModNetChannel channel;

    public C2SModPacket(ModNetChannel channel, Packet<?> packet) {
        this.channel = channel;
        this.channelId = channel.id();
        this.packet = packet;
    }

    public C2SModPacket(PacketIO buffer) {
        this.channelId = buffer.readId();
        this.channel = ModNetChannel.getChannel(this.channelId);
        this.packet = this.channel.getDecoder(buffer.readUnsignedShort()).apply(buffer);
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

    public ModNetChannel getChannel() {
        return this.channel;
    }
}
