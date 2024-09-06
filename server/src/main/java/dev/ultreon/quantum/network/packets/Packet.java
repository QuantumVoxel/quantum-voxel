package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.PacketIO;

public interface Packet<T extends PacketHandler> {
    void toBytes(PacketIO buffer);

    void handle(PacketContext ctx, T handler);
}
