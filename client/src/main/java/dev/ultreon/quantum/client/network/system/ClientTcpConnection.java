package dev.ultreon.quantum.client.network.system;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.c2s.C2SDisconnectPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Result;

public class ClientTcpConnection/* extends TcpConnection<ClientPacketHandler, ServerPacketHandler>*/ {
    private final QuantumClient client;

    private ClientTcpConnection(Client kryoClient, QuantumClient client) {
//        super(kryoClient, client);

        this.client = client;
    }

//    public static Result<ClientTcpConnection> connectToServer(String address, int port) {
//        try {
//            Client kryoClient = new Client(2 * 1024 * 1024, 2 * 1024 * 1024);
//            kryoClient.setKeepAliveTCP(ClientConfig.networkKeepAliveTime);
//            kryoClient.setName("Quantum:Multiplayer");
//            kryoClient.getKryo().setReferences(false);
//            kryoClient.getKryo().setRegistrationRequired(false);
//            kryoClient.getKryo().setDefaultSerializer(new PacketIOSerializerFactory());
//            kryoClient.start();
//            ClientTcpConnection connection = new ClientTcpConnection(kryoClient, QuantumClient.get());
//            connection.moveTo(PacketStages.LOGIN, new LoginClientPacketHandlerImpl(connection));
//            kryoClient.connect(ClientConfig.networkTimeout, address, port);
//            return Result.ok(connection);
//        } catch (IOException e) {
//            return Result.failure(e);
//        }
//    }

//    @Override
//    public void connected(Connection connection) {
//        super.connected(connection);
//
//        this.start();
//    }

    public static Result<ClientMemoryConnection> connectToLocalServer() {
        return Result.ok(new ClientMemoryConnection(QuantumClient.get(), Thread.currentThread()));
    }

//    @Override
//    protected boolean isRunning() {
//        return !client.isShutdown();
//    }
//
//    @Override
//    protected ServerPlayer getPlayer() {
//        return null; // Guaranteed to be null
//    }
//
//    @Override
//    protected Packet<ServerPacketHandler> getDisconnectPacket(String message) {
//        return new C2SDisconnectPacket<>(message);
//    }
//
//    @Override
//    public Result<Void> on3rdPartyDisconnect(String message) {
//        try {
//            this.close();
//        } catch (Exception e) {
//            if (this.isConnected()) {
//                QuantumClient.LOGGER.error("Failed to close connection", e);
//            }
//        }
//
//        client.onDisconnect(message);
//        return Result.ok(null);
//    }
//
//    @Override
//    public void onPing(long ping) {
//        this.ping = ping;
//    }
//
//    @Override
//    protected PacketData<ClientPacketHandler> getOurData(PacketStage stage) {
//        return stage.getClientPackets();
//    }
//
//    @Override
//    protected PacketData<ServerPacketHandler> getTheirData(PacketStage stage) {
//        return stage.getServerPackets();
//    }
}
