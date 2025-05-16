package dev.ultreon.quantum.registry;

import com.badlogic.gdx.utils.IntMap;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;

public class S2CRegistrySync implements Packet<LoginClientPacketHandler> {
    private final NamespaceID registryID;
    private final IntMap<NamespaceID> registryMap = new IntMap<>();

    public <T> S2CRegistrySync(Registry<T> registry) {
        this.registryID = registry.id();

        for (int i = 0; i < registry.size(); i++) {
            T object = registry.byRawId(i);
            NamespaceID namespaceID = registry.getId(object);
            this.registryMap.put(i, namespaceID);
        }
    }

    public IntMap<NamespaceID> getRegistryMap() {
        return this.registryMap;
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(this.registryMap.size);
        for (int i = 0; i < this.registryMap.size; i++) {
            buffer.writeId(this.registryMap.get(i));
        }
    }

    @Override
    public void handle(PacketContext ctx, LoginClientPacketHandler handler) {
        handler.onRegistrySync(this);
    }

    public NamespaceID getRegistryID() {
        return registryID;
    }
}
