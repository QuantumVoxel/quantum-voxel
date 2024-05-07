package dev.ultreon.quantum.network;

import com.google.errorprone.annotations.CheckReturnValue;
import dev.ultreon.quantum.network.api.packet.ClientEndpoint;
import dev.ultreon.quantum.network.api.packet.ModPacket;
import dev.ultreon.quantum.network.api.packet.ModPacketContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.c2s.C2SModPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CModPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Identifier;
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

public class NetworkChannel {
    private static final Map<Identifier, NetworkChannel> CHANNELS = new HashMap<>();
    private final Identifier key;
    private int curId;
    private final Reference2IntMap<Class<? extends ModPacket<?>>> idMap = new Reference2IntArrayMap<>();
    private final Map<Class<? extends ModPacket<?>>, BiConsumer<? extends ModPacket<?>, PacketIO>> encoders = new HashMap<>();
    private final Int2ReferenceMap<Function<PacketIO, ? extends ModPacket<?>>> decoders = new Int2ReferenceArrayMap<>();
    private final Map<Class<? extends ModPacket<?>>, BiConsumer<? extends ModPacket<?>, Supplier<ModPacketContext>>> consumers = new HashMap<>();

    @Environment(EnvType.CLIENT)
    private IConnection<ClientPacketHandler, ServerPacketHandler> c2sConnection;

    private NetworkChannel(Identifier key) {
        this.key = key;
    }

    public static NetworkChannel create(Identifier id) {
        NetworkChannel channel = new NetworkChannel(id);
        NetworkChannel.CHANNELS.put(id, channel);
        return channel;
    }

    @CheckReturnValue
    public static NetworkChannel getChannel(Identifier channelId) {
        return NetworkChannel.CHANNELS.get(channelId);
    }

    @Environment(EnvType.CLIENT)
    public void setC2sConnection(IConnection<ClientPacketHandler, ServerPacketHandler> connection) {
        this.c2sConnection = connection;
    }

    public Identifier id() {
        return this.key;
    }

    public <T extends ModPacket<T>> void register(Class<T> clazz, BiConsumer<T, PacketIO> encoder, Function<PacketIO, T> decoder, BiConsumer<T, Supplier<ModPacketContext>> packetConsumer) {
        this.curId++;
        this.idMap.put(clazz, this.curId);
        this.encoders.put(clazz, encoder);
        this.decoders.put(this.curId, decoder);
        this.consumers.put(clazz, packetConsumer);
    }

    public void queue(Runnable task) {
        this.c2sConnection.queue(task);
    }

    public <T extends ModPacket<T> & ClientEndpoint> void sendToPlayer(ServerPlayer player, ModPacket<T> modPacket) {
        player.connection.send(new S2CModPacket(this, modPacket));
    }

    public <T extends ModPacket<T> & ClientEndpoint> void sendToPlayers(List<ServerPlayer> players, ModPacket<T> modPacket) {
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = players.get(i);
            player.connection.send(new S2CModPacket(this, modPacket));
            if (i == players.size() - 1) {
                player.connection.send(new S2CModPacket(this, modPacket));
            }
        }
    }

    public <T extends ModPacket<T> & ClientEndpoint> void sendToServer(ModPacket<T> modPacket) {
        this.c2sConnection.send(new C2SModPacket(this, modPacket));
    }

    public Function<PacketIO, ? extends ModPacket<?>> getDecoder(int id) {
        return this.decoders.get(id);
    }

    public BiConsumer<? extends ModPacket<?>, PacketIO> getEncoder(Class<? extends ModPacket<?>> type) {
        return this.encoders.get(type);
    }

    public BiConsumer<? extends ModPacket<?>, Supplier<ModPacketContext>> getConsumer(Class<? extends ModPacket<?>> type) {
        return this.consumers.get(type);
    }

    public int getId(ModPacket<?> packet) {
        return this.idMap.getInt(packet.getClass());
    }
}
