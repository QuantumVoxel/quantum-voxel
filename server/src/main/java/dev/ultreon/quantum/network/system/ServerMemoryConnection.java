package dev.ultreon.quantum.network.system;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketData;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStage;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Result;
import dev.ultreon.quantum.util.Env;
import org.jetbrains.annotations.Nullable;

public class ServerMemoryConnection extends MemoryConnection<ServerPacketHandler, ClientPacketHandler> {
    private ServerPlayer player;

    public ServerMemoryConnection(@Nullable MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide, QuantumServer server) {
        super(otherSide, server);
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    protected void receive(Packet<? extends ServerPacketHandler> packet, @Nullable PacketListener resultListener) {
        QuantumServer.invoke(() -> {
            try {
                super.receive(packet, resultListener);
            } catch (Exception e) {
                CommonConstants.LOGGER.warn("Packet failed to receive!", e);
            }
        });
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(String message) {
        if (player != null)
            player.onDisconnect(message);
        return null;
    }

    @Override
    protected PacketContext createPacketContext() {
        return new PacketContext(player, this, Env.CLIENT);
    }

    @Override
    protected PacketData<ServerPacketHandler> getOurData(PacketStage stage) {
        return stage.getServerPackets();
    }

    @Override
    protected PacketData<ClientPacketHandler> getTheirData(PacketStage stage) {
        return stage.getClientPackets();
    }
}
