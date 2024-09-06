package dev.ultreon.quantum.server.dedicated;

import net.fabricmc.api.DedicatedServerModInitializer;

public interface DedicatedServerModInit extends DedicatedServerModInitializer {
    String ENTRYPOINT_KEY = "server";

    void onInitialize();
}
