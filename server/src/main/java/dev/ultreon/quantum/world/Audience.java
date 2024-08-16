package dev.ultreon.quantum.world;

import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.text.TextObject;

public interface Audience {
    void sendPacket(Packet<? extends ClientPacketHandler> s2CChunkUnloadPacket);

    void sendMessage(String message);

    void sendMessage(TextObject message);
}
