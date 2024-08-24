package dev.ultreon.quantum.network.client;

import dev.ultreon.quantum.network.packets.s2c.S2CLoginAcceptedPacket;

public interface LoginClientPacketHandler extends ClientPacketHandler {

    void onLoginAccepted(S2CLoginAcceptedPacket uuid);
}
