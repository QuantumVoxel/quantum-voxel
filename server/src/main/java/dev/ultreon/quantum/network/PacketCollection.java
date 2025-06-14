package dev.ultreon.quantum.network;

import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.network.packets.Packet;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a collection of packets.
 * Also contains methods for encoding, decoding and handling packets.
 *
 * @param <H> the packet handler type.
 */
public class PacketCollection<H extends PacketHandler> {
    private int id;
    private final Map<Class<? extends Packet<?>>, BiConsumer<Packet<?>, PacketIO>> encoders = new HashMap<>();
    private final Int2ObjectMap<Function<PacketIO, ? extends Packet<H>>> decoders = new Int2ObjectArrayMap<>();
    private final Map<Class<? extends Packet<?>>, BiConsumer<Packet<H>, Pair<PacketContext, H>>> handlers = new HashMap<>();
    private final Map<Class<? extends Packet<?>>, Integer> packet2id = new HashMap<>();

    /**
     * Adds a packet to this collection.
     *
     * @param type the type of the packet to add.
     * @param encoder the encoder to use.
     * @param decoder the decoder to use.
     * @param handler the handler to use.
     * @return the ID of the packet.
     */
    public int add(Class<? extends Packet<?>> type, BiConsumer<Packet<?>, PacketIO> encoder, Function<PacketIO, Packet<H>> decoder, BiConsumer<Packet<H>, Pair<PacketContext, H>> handler) {
        this.encoders.put(type, encoder);
        this.decoders.put(this.id, decoder);
        this.handlers.put(type, handler);
        this.packet2id.put(type, this.id);
        return this.id++;
    }

    /**
     * Encodes a packet to a buffer.
     *
     * @param packet the packet to encode.
     * @param buffer the buffer to encode to.
     * @throws PacketException if the packet is not registered.
     */
    public void encode(Packet<?> packet, PacketIO buffer) {
        @Nullable BiConsumer<Packet<?>, PacketIO> encoder = this.encoders.get(packet.getClass());
        if (encoder == null)
            throw new PacketException("Unknown packet: " + packet.getClass());
        encoder.accept(packet, buffer);
    }

    /**
     * Decodes a packet from a buffer
     *
     * @param id the ID of the packet to decode.
     * @param buffer the buffer to decode from.
     * @return the decoded packet.
     * @throws PacketException if the packet ID is unknown.
     */
    public Packet<H> decode(int id, PacketIO buffer) {
        Function<PacketIO, ? extends Packet<H>> decoder = this.decoders.get(id);
        if (decoder == null)
            throw new PacketException("Unknown packet ID: " + id);
        return decoder.apply(buffer);
    }

    /**
     * Handles a packet.
     *
     * @param packet the packet to handle.
     * @param params the parameters of the packet.
     * @throws PacketException if the packet is not registered.
     */
    public void handle(Packet<H> packet, Pair<PacketContext, H> params) {
        BiConsumer<Packet<H>, Pair<PacketContext, H>> handler = this.handlers.get(packet.getClass());
        if (handler == null)
            throw new PacketException("Unknown packet: " + packet.getClass());
        handler.accept(packet, params);
    }

    /**
     * Gets the ID of a packet.
     *
     * @param packet the packet to get the ID of.
     * @return the ID of the packet.
     * @throws PacketException if the packet is not registered.
     */
    public int getId(Packet<?> packet) {
        Integer id = this.packet2id.get(packet.getClass());
        if (id == null)
            throw new PacketException("Unknown packet: " + packet.getClass());
        return id;
    }
}
