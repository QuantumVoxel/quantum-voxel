package dev.ultreon.quantum.client.network;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.network.api.Network;
import dev.ultreon.quantum.network.api.PacketRegisterContext;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.api.packet.ServerEndpoint;
import dev.ultreon.quantum.network.packets.c2s.C2SModPacket;
import dev.ultreon.quantum.network.system.IConnection;

public abstract class ClientNetwork extends Network {
    protected ClientNetwork(String modId, String channelName) {
        super(modId, channelName);
    }

    @Override
    protected void registerPackets(PacketRegisterContext ctx) {

    }

    @Override
    public <T extends ModPacket<T> & ServerEndpoint> void sendToServer(T packet) {
        IConnection connection = QuantumClient.get().connection;
        if (connection != null) {
            connection.send(new C2SModPacket(this.channel, (ModPacket<T>) packet));
        }
    }

}
