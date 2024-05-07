package dev.ultreon.quantum.network.system;

import dev.ultreon.quantum.network.Networker;
import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.collect.Lists;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class TcpNetworker implements ConnectionReceiver, Networker {
    private final QuantumServer server;
    private final ServerSocket serverSocket;
    private final ServerThread thread;
    private final List<ServerTcpConnection> connections = Lists.newArrayList();

    public TcpNetworker(QuantumServer server, @Nullable InetAddress host, int port) throws IOException {
        this.server = server;
        this.thread = new ServerThread(host, port);
        this.serverSocket = new ServerSocket(port, 1, host);

        this.thread.start();
    }

    @Override
    public void close() throws IOException {
        this.thread.shutdown();
        this.serverSocket.close();
    }

    public void join() throws InterruptedException {
        this.thread.join();
    }

    @Override
    public Socket accept() throws IOException {
        return this.serverSocket.accept();
    }

    @Override
    public boolean isRunning() {
        return this.thread.isRunning();
    }

    @Override
    public List<ServerTcpConnection> getConnections() {
        return Collections.unmodifiableList(this.connections);
    }

    @Override
    public void tick() {
        for (ServerTcpConnection connection : this.connections) {
            connection.tick();
        }
    }

    private class ServerThread extends Thread {
        private final InetAddress host;
        private final int port;
        private boolean running;

        public ServerThread(@Nullable InetAddress host, int port) {
            super("ServerThread");
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            // Server network thread loop.
            running = true;

            TcpNetworker networker = TcpNetworker.this;
            while (running) {
                ServerTcpConnection connection = null;
                try(Socket accepted = networker.accept()) {
                    connection = new ServerTcpConnection(accepted, server);
                    this.connect(connection);
                } catch (IOException e) {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (IOException ex) {
                            e.addSuppressed(ex);
                        }
                    }

                    QuantumServer.LOGGER.error("Failed to accept connection.", e);
                }
            }
        }

        private void connect(ServerTcpConnection connection) {
            connections.add(connection);
            connection.start();
        }

        private void disconnect(ServerTcpConnection connection) {
            if (connections.contains(connection)) {
                connections.remove(connection);
                connection.disconnect("Disconnected");
                try {
                    connection.close();
                } catch (IOException e) {
                    QuantumServer.LOGGER.error("Failed to close connection.", e);
                }
            }
        }

        public void shutdown() {
            // Server network thread shutdown.
            this.running = false;
            this.interrupt();
        }
        public boolean isRunning() {
            return running;
        }

        public InetAddress getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
