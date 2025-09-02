package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.DimensionInfo;

import java.util.Objects;

public final class S2CChangeDimensionPacket implements Packet<InGameClientPacketHandler> {
    private final RegistryKey<DimensionInfo> dimension;

    public S2CChangeDimensionPacket(
            RegistryKey<DimensionInfo> dimension) {
        this.dimension = dimension;
    }

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

    public RegistryKey<DimensionInfo> dimension() {
        return dimension;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CChangeDimensionPacket) obj;
        return Objects.equals(this.dimension, that.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension);
    }

    @Override
    public String toString() {
        return "S2CChangeDimensionPacket[" +
               "dimension=" + dimension + ']';
    }

}
