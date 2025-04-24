package dev.ultreon.quantum.dedicated;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.Networker;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.CloseCodes;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/server")
public class JavaWebSocketServer implements Networker {
    private final DedicatedServer server;
    private final Map<Session, ServerConnection> connections = new ConcurrentHashMap<>();
    private boolean running = true;

    public JavaWebSocketServer() {
        this.server = DedicatedServer.get();
    }

    @OnOpen
    public void onOpen(Session session) {
        ServerConnection serverConnection = new ServerConnection(session, server);
        connections.put(session, serverConnection);
    }

    @OnMessage
    public void onMessage(byte[] b, boolean last, Session session) {
        ServerConnection serverConnection = connections.get(session);
        if (!last) {
            serverConnection.disconnect(CloseReason.CloseCodes.PROTOCOL_ERROR.getCode(), "Currently does not accept partial packets!");
        }
        serverConnection.onMessage(new ByteArrayInputStream(b));
    }

    @OnClose
    public void onClose(Session session) {
        CommonConstants.LOGGER.info("Disconnected: " + session.getId());
    }

    @OnError
    public void onError(Throwable t, Session session) {
        ServerConnection serverConnection = connections.get(session);
        serverConnection.disconnect(CloseCodes.VIOLATED_POLICY.getCode(), "Internal Error:\n" + t.getMessage());
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public List<? extends IConnection<ServerPacketHandler, ClientPacketHandler>> getConnections() {
        return List.copyOf(connections.values());
    }

    @Override
    public void tick() {

    }

    @Override
    public void close() throws IOException {
        this.running = false;
    }
}