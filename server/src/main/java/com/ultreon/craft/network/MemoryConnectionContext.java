package com.ultreon.craft.network;

import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.system.MemoryConnection;

public class MemoryConnectionContext {
    private static MemoryConnection<ClientPacketHandler, ServerPacketHandler> memoryConnection;

    public static MemoryConnection<ClientPacketHandler, ServerPacketHandler> get() {
        return memoryConnection;
    }

    public static void set(MemoryConnection<ClientPacketHandler, ServerPacketHandler> memoryConnection) {
        MemoryConnectionContext.memoryConnection = memoryConnection;
    }
}
