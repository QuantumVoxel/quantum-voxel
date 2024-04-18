package com.ultreon.quantum.network.packets;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.PacketHandler;

public abstract class Packet<T extends PacketHandler> {
    public Packet() {

    }

    public abstract void toBytes(PacketIO buffer);

    public abstract void handle(PacketContext ctx, T handler);
}
