package com.ultreon.craft.network.packets;

import com.ultreon.craft.network.PacketIO;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketHandler;

public abstract class Packet<T extends PacketHandler> {
    public Packet() {

    }

    public abstract void toBytes(PacketIO buffer);

    public abstract void handle(PacketContext ctx, T handler);
}
