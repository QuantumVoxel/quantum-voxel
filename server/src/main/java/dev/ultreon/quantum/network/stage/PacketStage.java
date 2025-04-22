package dev.ultreon.quantum.network.stage;

import dev.ultreon.quantum.network.PacketCollection;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;

import java.util.function.Function;

public abstract class PacketStage {
    private final PacketCollection<ClientPacketHandler> clientBoundList = new PacketCollection<>();
    private final PacketCollection<ServerPacketHandler> serverBoundList = new PacketCollection<>();
    private final PacketData<ClientPacketHandler> clientData;
    private final PacketData<ServerPacketHandler> serverData;

    /**
     * Constructs a new packet stage.
     */
    protected PacketStage() {
        this.registerPackets();
        this.clientData = new PacketData<>(this.clientBoundList);
        this.serverData = new PacketData<>(this.serverBoundList);
    }

    /**
     * Registers all packets in this packet stage.
     */
    public abstract void registerPackets();

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param decoder the packet decoder
     * @param typeGetter the type getter for the packet
     * @param <T> the type of the packet
     * @return the id of the packet
     */
    @SuppressWarnings("unchecked")
    protected <T extends Packet<? extends ServerPacketHandler>> int addServerBound(Function<PacketIO, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return this.serverBoundList.add(type, Packet::toBytes, t -> (Packet<ServerPacketHandler>) decoder.apply(t), (o, o2) -> o.handle(o2.getFirst(), o2.getSecond()));
    }

    /**
     * Adds a client-bound packet to this packet stage.
     *
     * @param decoder the packet decoder
     * @param typeGetter the type getter for the packet
     * @param <T> the type of the packet
     * @return the id of the packet
     */
    @SuppressWarnings("unchecked")
    protected <T extends Packet<?>> int addClientBound(Function<PacketIO, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return this.clientBoundList.add(type, Packet::toBytes, t -> (Packet<ClientPacketHandler>) decoder.apply(t), (o, o2) -> o.handle(o2.getFirst(), o2.getSecond()));
    }

    /**
     * @return the client bound packet data.
     */
    public PacketData<ClientPacketHandler> getClientPackets() {
        return this.clientData;
    }

    /**
     * @return the server bound packet data.
     */
    public PacketData<ServerPacketHandler> getServerPackets() {
        return this.serverData;
    }
}
