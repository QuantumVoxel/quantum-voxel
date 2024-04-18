package com.ultreon.quantum.network;

import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.system.MemoryConnection;

public class MemoryConnectionContext {
    private static MemoryConnection<ClientPacketHandler, ServerPacketHandler> memoryConnection;

    public static MemoryConnection<ClientPacketHandler, ServerPacketHandler> get() {
        return memoryConnection;
    }

    public static void set(MemoryConnection<ClientPacketHandler, ServerPacketHandler> memoryConnection) {
        MemoryConnectionContext.memoryConnection = memoryConnection;
    }
}
