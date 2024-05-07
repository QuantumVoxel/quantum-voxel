package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketHandler;
import dev.ultreon.quantum.network.PacketIO;

public abstract class Packet<T extends PacketHandler> {
    public Packet() {

    }

    public abstract void toBytes(PacketIO buffer);

    public abstract void handle(PacketContext ctx, T handler);
}
