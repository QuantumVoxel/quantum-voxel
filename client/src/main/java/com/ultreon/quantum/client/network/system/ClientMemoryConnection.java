package com.ultreon.quantum.client.network.system;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.PacketData;
import com.ultreon.quantum.network.PacketListener;
import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.stage.PacketStage;
import com.ultreon.quantum.network.system.MemoryConnection;
import com.ultreon.quantum.util.Result;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.Nullable;

public class ClientMemoryConnection extends MemoryConnection<ClientPacketHandler, ServerPacketHandler> {
    private final QuantumClient client;

    public ClientMemoryConnection(QuantumClient client) {
        super(null, client);
        this.client = client;
    }

    @Override
    protected void receive(Packet<? extends ClientPacketHandler> packet, @Nullable PacketListener resultListener) {
        QuantumClient.invoke(() -> super.receive(packet, resultListener));
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
