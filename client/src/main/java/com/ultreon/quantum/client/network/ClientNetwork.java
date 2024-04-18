package com.ultreon.quantum.client.network;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.network.system.IConnection;
import com.ultreon.quantum.network.api.Network;
import com.ultreon.quantum.network.api.PacketRegisterContext;
import com.ultreon.quantum.network.api.packet.ModPacket;
import com.ultreon.quantum.network.api.packet.ServerEndpoint;
import com.ultreon.quantum.network.packets.c2s.C2SModPacket;

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
            connection.send(new C2SModPacket(this.channel, packet));
        }
    }

}
