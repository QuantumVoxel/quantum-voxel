package dev.ultreon.quantapi.networking.impl.packet;

import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantapi.networking.impl.ModPacketContext;
import dev.ultreon.quantum.client.network.InGameClientPacketHandlerImpl;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.NamespaceID;

public class S2CModPacket extends dev.ultreon.quantum.network.packets.Packet<InGameClientPacketHandlerImpl> {
    private final NamespaceID channelId;
    private final Packet<?> packet;
    private final ModNetChannel channel;

    public S2CModPacket(ModNetChannel channel, Packet<?> packet) {
        this.channel = channel;
        this.channelId = channel.id();
        this.packet = packet;
    }

    public S2CModPacket(PacketIO buffer) {
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
    public void handle(PacketContext ctx, InGameClientPacketHandlerImpl handler) {
        this.packet.handlePacket(() -> new ModPacketContext(this.channel, null, handler.context().getConnection(), Env.CLIENT));
    }
}
