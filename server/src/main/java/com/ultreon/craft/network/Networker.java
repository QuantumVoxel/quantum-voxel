package com.ultreon.craft.network;

import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.system.IConnection;

import java.io.Closeable;
import java.util.List;

public interface Networker extends Closeable {

    boolean isRunning();

    List<? extends IConnection<ServerPacketHandler, ClientPacketHandler>> getConnections();

    void tick();
}
