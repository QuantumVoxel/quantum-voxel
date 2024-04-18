package com.ultreon.craft.network;

import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.server.LoginServerPacketHandler;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.system.MemoryConnection;
import com.ultreon.craft.network.system.ServerMemoryConnection;
import com.ultreon.craft.server.UltracraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MemoryNetworker implements Networker {
    private final UltracraftServer server;
    private MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide;
    private @Nullable ServerMemoryConnection connection;

    public MemoryNetworker(UltracraftServer server, MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide) {
        super();
        this.server = server;
        this.otherSide = otherSide;
        this.connection = new ServerMemoryConnection(otherSide, server);
        this.connection.initiate(new LoginServerPacketHandler(this.server, connection), null);
    }

    @Override
    public boolean isRunning() {
        return server.isRunning();
    }

    @Override
    public List<ServerMemoryConnection> getConnections() {
        var conn = this.connection;
        if (conn == null) {
            return Collections.emptyList();
        }
        return List.of(conn);
    }

    @Override
    public void tick() {
        var conn = connection;
        if (conn != null) {
            conn.tick();
        }
    }

    public MemoryConnection<ClientPacketHandler, ServerPacketHandler> getOtherSide() {
        return otherSide;
    }

    @Override
    public void close() {

    }

    public void setOtherSide(MemoryConnection<ClientPacketHandler, ServerPacketHandler> connection) {
        this.otherSide = connection;
    }
}
