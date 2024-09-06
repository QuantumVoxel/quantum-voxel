package dev.ultreon.quantum.world;

import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.text.TextObject;

/**
 * Provides an abstraction for an audience that can receive packets and messages.
 * Classes implementing this interface are intended to represent an audience
 * that can be interacted with through network packets and text messages.
 */
public interface Audience {
    void sendPacket(Packet<? extends ClientPacketHandler> packet);

    void sendMessage(String message);

    void sendMessage(TextObject message);
}
