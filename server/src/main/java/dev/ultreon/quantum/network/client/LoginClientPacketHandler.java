package dev.ultreon.quantum.network.client;

import dev.ultreon.quantum.network.packets.s2c.S2CLoginAcceptedPacket;
import dev.ultreon.quantum.registry.S2CRegistrySync;
import dev.ultreon.quantum.server.S2CRegistriesSync;

public interface LoginClientPacketHandler extends ClientPacketHandler {

    void onLoginAccepted(S2CLoginAcceptedPacket uuid);

    void onRegistrySync(S2CRegistrySync packet);

    void onRegistriesSync(S2CRegistriesSync packet);
}
