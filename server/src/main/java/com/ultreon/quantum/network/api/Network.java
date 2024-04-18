package com.ultreon.quantum.network.api;

import com.ultreon.quantum.network.NetworkChannel;
import com.ultreon.quantum.network.api.packet.ClientEndpoint;
import com.ultreon.quantum.network.api.packet.ModPacket;
import com.ultreon.quantum.network.api.packet.ServerEndpoint;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public abstract class Network {
    private final String modId;
    private final String channelName;

    protected NetworkChannel channel;

    @ApiStatus.Internal
    protected Network(String modId, String channelName) {
        this.modId = modId;
        this.channelName = channelName;

        NetworkManager.registerNetwork(this);
    }

    @Deprecated
    @ApiStatus.Internal
    protected Network(String modId, String channelName, @Deprecated int ignoredVersion) {
        this(modId, channelName);
    }

    public final void init() {
        int id = 0;
        this.channel = NetworkChannel.create(new Identifier(this.namespace(), this.channelName()));

        this.registerPackets(new PacketRegisterContext(this.channel, id));
    }

    protected abstract void registerPackets(PacketRegisterContext ctx);

    public final String channelName() {
        return this.channelName;
    }

    public final String namespace() {
        return this.modId;
    }

    public <T extends ModPacket<T> & ClientEndpoint> void sendToClient(ModPacket<T> modPacket, ServerPlayer player) {

    }

    public <T extends ModPacket<T> & ServerEndpoint> void sendToServer(T message) {

    }

    public final Identifier getId() {
        return new Identifier(this.namespace(), this.channelName());
    }
}
