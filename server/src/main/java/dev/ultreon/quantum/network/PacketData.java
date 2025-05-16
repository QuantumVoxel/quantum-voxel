package dev.ultreon.quantum.network;

import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.debug.timing.Timing;
import dev.ultreon.quantum.network.packets.Packet;

public class PacketData<T extends PacketHandler> {
    private final PacketCollection<T> collection;

    public PacketData(PacketCollection<T> collection) {
        this.collection = collection;
    }

    public Packet<T> decode(int id, PacketIO buffer) {
        Timing.start("packet_decode:" + id);
        Packet<T> decode = this.collection.decode(id, buffer);
        Timing.end("packet_decode:" + id);
        return decode;
    }

    public void encode(Packet<?> packet, PacketIO buffer) {
        Timing.start("packet_encode:" + packet.getClass().getName());
        this.collection.encode(packet, buffer);
        Timing.end("packet_encode:" + packet.getClass().getName());
    }

    public void handle(Packet<T> packet, PacketContext context, T listener) {
        Timing.start("packet_handle:" + packet.getClass().getName());
        this.collection.handle(packet, new Pair<>(context, listener));
        Timing.end("packet_handle:" + packet.getClass().getName());
    }

    public int getId(Packet<? extends T> msg) {
        return this.collection.getId(msg);
    }
}
