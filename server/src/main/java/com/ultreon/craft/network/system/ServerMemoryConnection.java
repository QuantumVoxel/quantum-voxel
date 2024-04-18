package com.ultreon.craft.network.system;

import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketData;
import com.ultreon.craft.network.PacketListener;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.stage.PacketStage;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.Result;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.Nullable;

public class ServerMemoryConnection extends MemoryConnection<ServerPacketHandler, ClientPacketHandler> {
    private ServerPlayer player;

    public ServerMemoryConnection(@Nullable MemoryConnection<ClientPacketHandler, ServerPacketHandler> otherSide, UltracraftServer server) {
        super(otherSide, server);
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    protected void receive(Packet<? extends ServerPacketHandler> packet, @Nullable PacketListener resultListener) {
        UltracraftServer.invoke(() -> super.receive(packet, resultListener));
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
