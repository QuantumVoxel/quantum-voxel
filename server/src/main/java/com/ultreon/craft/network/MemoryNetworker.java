package com.ultreon.craft.network;

import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.system.MemoryConnection;
import com.ultreon.craft.network.system.ServerMemoryConnection;
import com.ultreon.craft.server.UltracraftServer;

import java.io.IOException;
import java.util.List;

public class MemoryNetworker implements Networker {
    private final UltracraftServer server;
    private final MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide;
    private final ServerMemoryConnection connection;

    public MemoryNetworker(UltracraftServer server, MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide) {
        super();
        this.server = server;
        this.otherSide = otherSide;

        this.connection = new ServerMemoryConnection(otherSide, server);
    }

    @Override
    public boolean isRunning() {
        return server.isRunning();
    }

    @Override
    public List<ServerMemoryConnection> getConnections() {
        return List.of(this.connection);
    }

    @Override
    public void tick() {
        connection.tick();
    }

    public MemoryConnection<ClientPacketHandler, ServerPacketHandler> getOtherSide() {
        return otherSide;
    }

    @Override
    public void close() {

    }
}
