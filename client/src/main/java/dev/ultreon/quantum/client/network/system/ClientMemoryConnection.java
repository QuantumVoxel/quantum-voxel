package dev.ultreon.quantum.client.network.system;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.network.system.MemoryConnection;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientMemoryConnection extends MemoryConnection<ClientPacketHandler, ServerPacketHandler> {
    private final QuantumClient client;

    public ClientMemoryConnection(QuantumClient client, Thread thread) {
        super(null, client, Env.CLIENT, client.registries);
        this.client = client;
    }

    @Override
    protected void received(@NotNull Packet<? extends ClientPacketHandler> packet, @Nullable PacketListener resultListener) {
        if (packet == null) {
            CommonConstants.LOGGER.warn("Received null packet!", new Exception());
            return;
        }
        QuantumClient.invoke(() -> super.received(packet, resultListener));
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        CommonConstants.LOGGER.info("Received disconnect with message: " + message);

        this.connected = false;
        this.client.onDisconnect(message, true);
        return null;
    }

    @Override
    protected PacketContext createPacketContext() {
        return new PacketContext(null, this, Env.SERVER);
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
