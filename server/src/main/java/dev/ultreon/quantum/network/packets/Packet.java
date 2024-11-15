package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.PacketIO;

public interface Packet<T extends PacketHandler> {
    /**
     * Serializes the packet data into a byte buffer.
     *
     * @param buffer The {@link PacketIO} object that will contain the serialized packet data.
     */
    void toBytes(PacketIO buffer);

    /**
     * Handles the packet.
     *
     * @param ctx The context in which the packet is being handled, providing necessary environment details.
     * @param handler The handler specific to the packet type, responsible for executing the handling logic.
     */
    void handle(PacketContext ctx, T handler);
}
