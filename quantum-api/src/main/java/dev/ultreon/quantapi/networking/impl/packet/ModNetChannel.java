package dev.ultreon.quantapi.networking.impl.packet;

import com.google.errorprone.annotations.CheckReturnValue;
import dev.ultreon.quantapi.networking.api.INetChannel;
import dev.ultreon.quantapi.networking.api.IPacketContext;
import dev.ultreon.quantapi.networking.api.packet.IClientEndpoint;
import dev.ultreon.quantapi.networking.api.packet.IServerEndpoint;
import dev.ultreon.quantapi.networking.api.packet.Packet;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.NamespaceID;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModNetChannel implements INetChannel {
    private static final Map<NamespaceID, ModNetChannel> CHANNELS = new HashMap<>();
    private final NamespaceID key;
    private int curId;
    private final Reference2IntMap<Class<? extends Packet<?>>> idMap = new Reference2IntArrayMap<>();
    private final Map<Class<? extends Packet<?>>, BiConsumer<? extends Packet<?>, PacketIO>> encoders = new HashMap<>();
    private final Int2ReferenceMap<Function<PacketIO, ? extends Packet<?>>> decoders = new Int2ReferenceArrayMap<>();
    private final Map<Class<? extends Packet<?>>, BiConsumer<? extends Packet<?>, Supplier<IPacketContext>>> consumers = new HashMap<>();

    @Environment(EnvType.CLIENT)
    private IConnection<ClientPacketHandler, ServerPacketHandler> c2sConnection;

    private ModNetChannel(NamespaceID key) {
        this.key = key;
    }

    public static ModNetChannel create(NamespaceID id) {
        ModNetChannel channel = new ModNetChannel(id);
        ModNetChannel.CHANNELS.put(id, channel);
        return channel;
    }

    @CheckReturnValue
    public static ModNetChannel getChannel(NamespaceID channelId) {
        return ModNetChannel.CHANNELS.get(channelId);
    }

    @Environment(EnvType.CLIENT)
    public void setC2sConnection(IConnection<ClientPacketHandler, ServerPacketHandler> connection) {
        this.c2sConnection = connection;
    }

    @Override
    public NamespaceID id() {
        return this.key;
    }

    public <T extends Packet<T>> void register(Class<T> clazz, BiConsumer<T, PacketIO> encoder, Function<PacketIO, T> decoder, BiConsumer<T, Supplier<IPacketContext>> packetConsumer) {
        this.curId++;
        this.idMap.put(clazz, this.curId);
        this.encoders.put(clazz, encoder);
        this.decoders.put(this.curId, decoder);
        this.consumers.put(clazz, packetConsumer);
    }

    public void queue(Runnable task) {
        this.c2sConnection.queue(task);
    }

    @Override
    public <T extends Packet<T> & IClientEndpoint> void sendToClient(ServerPlayer player, T modPacket) {
        player.connection.send(new S2CModPacket(this, modPacket));
    }

    @Override
    public <T extends Packet<T> & IClientEndpoint> void sendToClients(List<ServerPlayer> players, T modPacket) {
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            player.connection.send(new S2CModPacket(this, modPacket));
            if (i == players.size() - 1) {
                player.connection.send(new S2CModPacket(this, modPacket));
            }
        }
    }

    @Override
    public <T extends Packet<T> & IServerEndpoint> void sendToServer(T modPacket) {
        this.c2sConnection.send(new C2SModPacket(this, modPacket));
    }

    public Function<PacketIO, ? extends Packet<?>> getDecoder(int id) {
        return this.decoders.get(id);
    }

    public BiConsumer<? extends Packet<?>, PacketIO> getEncoder(Class<? extends Packet<?>> type) {
        return this.encoders.get(type);
    }

    public BiConsumer<? extends Packet<?>, Supplier<IPacketContext>> getConsumer(Class<? extends Packet<?>> type) {
        return this.consumers.get(type);
    }

    public int getId(Packet<?> packet) {
        return this.idMap.getInt(packet.getClass());
    }
}
