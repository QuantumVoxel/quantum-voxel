package dev.ultreon.quantum.network;

import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.s2c.S2CDisconnectPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.*;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

/**
 * Connections for the server.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see ServerMemoryConnection
 * @see ServerTcpConnection
 */
public class ServerConnections {
    private static final Map<NamespaceID, NetworkChannel> CHANNELS = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConnections.class);
    private final QuantumServer server;

    private boolean running;
    private Networker networker;

    /**
     * Constructs a new {@link ServerConnections} for the specified {@link QuantumServer}.
     *
     * @param server the server to host.
     */
    public ServerConnections(QuantumServer server) {
        this.server = server;
        this.running = true;
    }

    /**
     * Registers a new channel.
     *
     * @param id the identifier of the channel
     * @return the new channel
     */
    public static NetworkChannel registerChannel(NamespaceID id) {
        NetworkChannel channel = NetworkChannel.create(id);
        ServerConnections.CHANNELS.put(id, channel);
        return channel;
    }

    /**
     * Get all registered network channels.
     *
     * @return the collection of network channels
     */
    public static Collection<NetworkChannel> getChannels() {
        return Collections.unmodifiableCollection(ServerConnections.CHANNELS.values());
    }

    /**
     * Get a channel by its identifier.
     *
     * @param namespaceID the identifier
     * @return the channel or {@code null} if not found
     */
    public static NetworkChannel getChannel(NamespaceID namespaceID) {
        return ServerConnections.CHANNELS.get(namespaceID);
    }

    /**
     * Starts a memory server using the provided configuration.
     *
     * @return the local address of the started memory server
     */
    public MemoryNetworker startMemoryServer(MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide) {
        MemoryNetworker networker;
        synchronized (this) {
            networker = new MemoryNetworker(server, otherSide);
            this.networker = networker;
        }

        return networker;
    }

    /**
     * Starts a TCP server at the specified address and port.
     *
     * @param address the IP address to bind the server to, or null to bind to all available network interfaces
     * @param port    the port number to listen on
     */
    public TcpNetworker startTcpServer(@Nullable InetAddress address, int port) throws ServerHostingException {
        TcpNetworker networker;
        synchronized (this) {
            try {
                networker = new TcpNetworker(server, address, port);
                this.networker = networker;
            } catch (IOException e) {
                throw new ServerHostingException(e);
            }
        }

        return networker;
    }

    public void tick() {
        synchronized (this) {
            Iterator<? extends IConnection<ServerPacketHandler, ClientPacketHandler>> iterator = this.networker.getConnections().iterator();

            while (true) {
                IConnection<ServerPacketHandler, ClientPacketHandler> connection;
                do {
                    if (!iterator.hasNext()) {
                        return;
                    }

                    connection = iterator.next();
                } while (connection.isConnecting());

                if (connection.isConnected()) {
                    try {
                        connection.tick();
                    } catch (Exception e) {
                        if (connection.isMemoryConnection()) {
                            this.server.crash(new RuntimeException("Failed to tick packet", e));
                        }

                        ServerConnections.LOGGER.warn("Failed to handle packet:", e);
                        String message = "Server failed to tick the connection";
                        connection.send(new S2CDisconnectPacket<>(message));
                        connection.disconnect(message);
                        connection.setReadOnly();
                    }
                } else {
                    iterator.remove();
                    connection.on3rdPartyDisconnect("Connection lost");
                }
            }
        }
    }

    /**
     * Get the server instance.
     *
     * @return the server instance
     */
    public QuantumServer getServer() {
        return this.server;
    }

    public void stop() {
        this.running = false;
    }

    /**
     * Check if the server is running.
     *
     * @return {@code true} if the server is running, {@code false} otherwise
     */
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public String toString() {
        return "ServerConnections%s";
    }
}
