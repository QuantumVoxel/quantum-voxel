package dev.ultreon.quantum.network.api.packet;

import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.system.IConnection;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public abstract class ModPacket<T extends ModPacket<T>> {
    protected abstract boolean handle(Supplier<ModPacketContext> context);

    public final boolean handlePacket(Supplier<ModPacketContext> context) {
        try {
            this.handle(context);
        } catch (Throwable throwable) {
            IConnection.LOGGER.error("Couldn't handle packet:", throwable);
        }
        return true;
    }

    public abstract void toBytes(PacketIO buffer);
}
