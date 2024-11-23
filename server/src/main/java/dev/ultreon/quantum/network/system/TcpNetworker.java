package dev.ultreon.quantum.network.system;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import dev.ultreon.quantum.network.Networker;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.server.LoginServerPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TcpNetworker extends Listener implements Networker {
    private final QuantumServer server;
    private final Map<Connection, ServerTcpConnection> connections = new HashMap<>();
    private final Server kryoServer;

    public TcpNetworker(QuantumServer server, @Nullable InetAddress host, int port) throws IOException {
        this.server = server;

        this.kryoServer = new Server(16 * 1024 * 1024, 16 * 1024 * 1024);
        this.kryoServer.addListener(this);
        this.kryoServer.getKryo().setReferences(false);
        this.kryoServer.getKryo().setRegistrationRequired(false);
        this.kryoServer.getKryo().setDefaultSerializer(new PacketIOSerializerFactory());

        this.kryoServer.bind(new InetSocketAddress(host, port), null);

        this.kryoServer.start();
    }

    @Override
    public void connected(Connection connection) {
        super.connected(connection);

        if (this.connections.containsKey(connection)) return;

        connection.setName("QuantumConn:" + connection.getRemoteAddressTCP().getAddress());

        ServerTcpConnection conn = new ServerTcpConnection(connection, this.kryoServer, this.server);
        conn.moveTo(PacketStages.LOGIN, new LoginServerPacketHandler(this.server, conn));
        this.connections.put(connection, conn);
    }

    @Override
    public void received(Connection connection, Object object) {
        super.received(connection, object);
    }

    @Override
    public void disconnected(Connection connection) {
        super.disconnected(connection);

        this.connections.remove(connection);
    }

    @Override
    public void close() throws IOException {
        for (ServerTcpConnection connection : this.connections.values()) {
            connection.disconnect("Server shutting down");
        }

        this.kryoServer.close();
    }

    @Override
    public boolean isRunning() {
        return this.kryoServer.getUpdateThread().isAlive();
    }

    @Override
    public List<? extends IConnection<ServerPacketHandler, ClientPacketHandler>> getConnections() {
        return this.connections.values().stream().map((c) -> (IConnection<ServerPacketHandler, ClientPacketHandler>) c).toList();
    }

    @Override
    public void tick() {
        for (ServerTcpConnection connection : this.connections.values()) {
            connection.tick();
        }
    }

}
