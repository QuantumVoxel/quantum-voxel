package dev.ultreon.quantum.network;

import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.network.system.ServerTcpConnection;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;

public interface Networker extends Closeable {

    boolean isRunning();

    List<? extends IConnection<ServerPacketHandler, ClientPacketHandler>> getConnections();

    void tick();
}
