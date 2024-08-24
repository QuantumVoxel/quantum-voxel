package dev.ultreon.quantapi.networking.api.packet;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantapi.networking.api.IPacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.system.IConnection;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public abstract sealed class Packet<T extends Packet<T>> permits BiDirectionalPacket, PacketToClient, PacketToServer {
    protected abstract boolean handle(Supplier<IPacketContext> context);

    @CanIgnoreReturnValue
    public final boolean handlePacket(Supplier<IPacketContext> context) {
        try {
            this.handle(context);
        } catch (Throwable throwable) {
            IConnection.LOGGER.error("Couldn't handle packet:", throwable);
        }
        return true;
    }

    public abstract void toBytes(PacketIO buffer);
}
