package com.ultreon.quantum.network;

import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.system.IConnection;

import java.io.Closeable;
import java.util.List;

public interface Networker extends Closeable {

    boolean isRunning();

    List<? extends IConnection<ServerPacketHandler, ClientPacketHandler>> getConnections();

    void tick();
}
