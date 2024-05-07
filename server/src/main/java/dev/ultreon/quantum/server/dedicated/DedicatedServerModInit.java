package dev.ultreon.quantum.server.dedicated;

public interface DedicatedServerModInit {
    String ENTRYPOINT_KEY = "server-init";

    void onInitialize();
}
