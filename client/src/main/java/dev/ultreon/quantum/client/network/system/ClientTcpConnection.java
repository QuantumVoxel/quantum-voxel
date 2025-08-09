package dev.ultreon.quantum.client.network.system;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.network.LoginClientPacketHandlerImpl;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.BundlePacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.c2s.C2SBundlePacket;
import dev.ultreon.quantum.network.packets.c2s.C2SDisconnectPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.network.system.PacketIOSerializerFactory;
import dev.ultreon.quantum.network.system.TcpConnection;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Result;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ClientTcpConnection extends TcpConnection<ClientPacketHandler, ServerPacketHandler> {
    private final QuantumClient client;

    private ClientTcpConnection(Client kryoClient, QuantumClient client) {
        super(kryoClient, client);

        this.client = client;
    }

    public static Result<ClientTcpConnection> connectToServer(String address, int port, Runnable success, Consumer<Throwable> error) {
        try {
            Client kryoClient = new Client(2 * 1024 * 1024, 2 * 1024 * 1024);
            kryoClient.setKeepAliveTCP(ClientConfiguration.networkKeepAliveTime.getValue());
            kryoClient.setName("Quantum:Multiplayer");
            kryoClient.getKryo().setReferences(false);
            kryoClient.getKryo().setRegistrationRequired(false);
            ClientTcpConnection connection = new ClientTcpConnection(kryoClient, QuantumClient.get());
            kryoClient.getKryo().setDefaultSerializer(new PacketIOSerializerFactory(QuantumClient.get().registries));
            kryoClient.start();
            connection.moveTo(PacketStages.LOGIN, new LoginClientPacketHandlerImpl(connection));
            kryoClient.connect(ClientConfiguration.networkTimeout.getValue(), address, port);
            success.run();
            QuantumClient.LOGGER.info("Connected to server at {}:{}", address, port);
            return Result.ok(connection);
        } catch (IOException e) {
            error.accept(e);
            QuantumClient.LOGGER.error("Failed to connect to server", e);
            return Result.failure(e);
        }
    }

    @Override
    public void connected(Connection connection) {
        super.connected(connection);

        this.start();
    }

    public static Result<ClientMemoryConnection> connectToLocalServer() {
        return Result.ok(new ClientMemoryConnection(QuantumClient.get(), Thread.currentThread()));
    }

    @Override
    protected boolean isRunning() {
        return !client.isShutdown();
    }

    @Override
    protected ServerPlayer getPlayer() {
        return null; // Guaranteed to be null
    }

    @Override
    protected Packet<ServerPacketHandler> getDisconnectPacket(int code, String message) {
        return new C2SDisconnectPacket(code, message);
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int code, String message) {
        try {
            this.close();
        } catch (Exception e) {
            if (this.isConnected()) {
                QuantumClient.LOGGER.error("Failed to close connection", e);
            }
        }

        client.onDisconnect(message, false);
        return Result.ok(null);
    }

    @Override
    public void update() {

    }

    @Override
    public void onPing(long ping) {
        this.ping = ping;
    }

    @Override
    public BundlePacket<ServerPacketHandler> bundle(List<Packet<ServerPacketHandler>> packets) {
        return new C2SBundlePacket(packets);
    }

    @Override
    protected PacketData<ClientPacketHandler> getOurData(PacketStage stage) {
        return stage.getClientPackets();
    }

    @Override
    protected PacketData<ServerPacketHandler> getTheirData(PacketStage stage) {
        return stage.getServerPackets();
    }
}
