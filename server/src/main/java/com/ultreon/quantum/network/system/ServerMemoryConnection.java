package com.ultreon.quantum.network.system;

import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.PacketData;
import com.ultreon.quantum.network.PacketListener;
import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.stage.PacketStage;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.util.Result;
import net.fabricmc.api.EnvType;
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
        QuantumServer.invoke(() -> super.receive(packet, resultListener));
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(String message) {
        if (player != null)
            player.onDisconnect(message);
        return null;
    }

    @Override
    protected PacketContext createPacketContext() {
        return new PacketContext(player, this, EnvType.CLIENT);
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
