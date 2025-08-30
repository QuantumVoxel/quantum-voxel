package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.BundlePacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class C2SBundlePacket implements BundlePacket<ServerPacketHandler> {
    private final List<Packet<ServerPacketHandler>> packets;

    public C2SBundlePacket(List<Packet<ServerPacketHandler>> packets) {
        this.packets = packets;
    }

    public static C2SBundlePacket read(ServerPacketHandler handler, PacketIO buffer) {
        ArrayList<Packet<ServerPacketHandler>> packets = new ArrayList<>();
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            int packetId = buffer.readVarInt();
            packets.add(handler.connection().getStage().getServerPackets().decode(handler, packetId, buffer));
        }
        return new C2SBundlePacket(packets);
    }

    public List<Packet<ServerPacketHandler>> getPackets() {
        return packets;
    }

    @Override
    public void toBytes(PacketHandler handler, PacketIO buffer) {
        buffer.writeVarInt(packets.size());
        for (Packet<ServerPacketHandler> packet : packets) {
            buffer.writeVarInt(handler.connection().getStage().getServerPackets().getId(packet));
            packet.toBytes(handler, buffer);
        }
    }

    @Override
    public void toBytes(PacketIO buffer) {
        throw new UnsupportedOperationException("Use toBytes(PacketHandler, PacketIO)");
    }

    @Override
    public void handle(PacketContext ctx, ServerPacketHandler handler) {
        for (Packet<ServerPacketHandler> packet : packets) {
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
        C2SBundlePacket that = (C2SBundlePacket) o;
        return Objects.equals(packets, that.packets);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packets);
    }
}
