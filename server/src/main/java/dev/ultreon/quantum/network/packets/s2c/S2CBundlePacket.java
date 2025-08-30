package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.BundlePacket;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class S2CBundlePacket implements BundlePacket<ClientPacketHandler> {
    private final List<Packet<ClientPacketHandler>> packets;

    public S2CBundlePacket(List<Packet<ClientPacketHandler>> packets) {
        this.packets = packets;
    }

    public static S2CBundlePacket read(ClientPacketHandler handler, PacketIO buffer) {
        ArrayList<Packet<ClientPacketHandler>> packets = new ArrayList<>();
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            int packetId = buffer.readVarInt();
            packets.add(handler.connection().getStage().getClientPackets().decode(handler, packetId, buffer));
        }
        return new S2CBundlePacket(packets);
    }

    public List<Packet<ClientPacketHandler>> getPackets() {
        return packets;
    }

    @Override
    public void toBytes(PacketHandler handler, PacketIO buffer) {
        buffer.writeVarInt(packets.size());
        for (Packet<ClientPacketHandler> packet : packets) {
            buffer.writeVarInt(handler.connection().getStage().getClientPackets().getId(packet));
            packet.toBytes(handler, buffer);
        }
    }

    @Override
    public void toBytes(PacketIO buffer) {
        throw new UnsupportedOperationException("Use toBytes(PacketHandler, PacketIO)");
    }

    @Override
    public void handle(PacketContext ctx, ClientPacketHandler handler) {
        for (Packet<ClientPacketHandler> packet : packets) {
            packet.handle(ctx, handler);
        }
    }

    @Override
    public String toString() {
        return "S2CAbilitiesPacket" + packets;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        S2CBundlePacket that = (S2CBundlePacket) o;
        return Objects.equals(packets, that.packets);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packets);
    }
}
