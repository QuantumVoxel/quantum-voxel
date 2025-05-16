package dev.ultreon.quantum.server;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.util.NamespaceID;

public class S2CRegistriesSync implements Packet<LoginClientPacketHandler> {
    private final Array<NamespaceID> registries = new Array<>(NamespaceID.class);

    public S2CRegistriesSync(Registry<Registry<?>> registries) {
        for (int i = 0; i < registries.size(); i++) {
            Registry<?> obj = registries.byRawId(i);
            if (obj == null || obj.isSyncDisabled()) continue;
            this.registries.set(i, registries.getId(obj));
        }
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(this.registries.size);
        for (NamespaceID entry : this.registries.items) {
            buffer.writeId(entry);
        }
    }

    @Override
    public void handle(PacketContext ctx, LoginClientPacketHandler handler) {
        handler.onRegistriesSync(this);
    }

    public Array<NamespaceID> getRegistries() {
        return this.registries;
    }
}
