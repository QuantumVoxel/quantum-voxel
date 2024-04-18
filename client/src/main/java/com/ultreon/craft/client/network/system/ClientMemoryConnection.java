package com.ultreon.craft.client.network.system;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketData;
import com.ultreon.craft.network.PacketListener;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;
import com.ultreon.craft.network.stage.PacketStage;
import com.ultreon.craft.network.system.MemoryConnection;
import com.ultreon.craft.util.Result;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.Nullable;

public class ClientMemoryConnection extends MemoryConnection<ClientPacketHandler, ServerPacketHandler> {
    private final UltracraftClient client;

    public ClientMemoryConnection(UltracraftClient client) {
        super(null, client);
        this.client = client;
    }

    @Override
    protected void receive(Packet<? extends ClientPacketHandler> packet, @Nullable PacketListener resultListener) {
        UltracraftClient.invoke(() -> super.receive(packet, resultListener));
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(String message) {
        this.client.onDisconnect(message);
        return null;
    }

    @Override
    protected PacketContext createPacketContext() {
        return new PacketContext(null, this, EnvType.SERVER);
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
