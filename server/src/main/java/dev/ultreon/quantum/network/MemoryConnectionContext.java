package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.MemoryConnection;

public class MemoryConnectionContext {
    private static MemoryConnection<ClientPacketHandler, ServerPacketHandler> memoryConnection;

    public static MemoryConnection<ClientPacketHandler, ServerPacketHandler> get() {
        return memoryConnection;
    }

    public static void set(MemoryConnection<ClientPacketHandler, ServerPacketHandler> memoryConnection) {
        MemoryConnectionContext.memoryConnection = memoryConnection;
    }
}
