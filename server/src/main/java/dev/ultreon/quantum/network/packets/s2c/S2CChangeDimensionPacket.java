package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.DimensionInfo;

public record S2CChangeDimensionPacket(
        RegistryKey<DimensionInfo> dimension) implements Packet<InGameClientPacketHandler> {

    public static S2CChangeDimensionPacket read(PacketIO buffer) {
        NamespaceID dimId = buffer.readId();
        RegistryKey<DimensionInfo> dimension = RegistryKey.of(RegistryKeys.DIMENSION, dimId);

        return new S2CChangeDimensionPacket(dimension);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(dimension.id());
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChangeDimension(ctx, this);
    }
}
