package com.ultreon.quantum.network.client;

import java.util.UUID;

public interface LoginClientPacketHandler extends ClientPacketHandler {

    void onLoginAccepted(UUID uuid);
}
