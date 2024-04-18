package com.ultreon.quantum.network;

import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.server.LoginServerPacketHandler;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.system.MemoryConnection;
import com.ultreon.quantum.network.system.ServerMemoryConnection;
import com.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MemoryNetworker implements Networker {
    private final QuantumServer server;
    private MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide;
    private @Nullable ServerMemoryConnection connection;

    public MemoryNetworker(QuantumServer server, MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide) {
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
