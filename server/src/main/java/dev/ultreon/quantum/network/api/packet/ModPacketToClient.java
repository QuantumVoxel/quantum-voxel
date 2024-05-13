package dev.ultreon.quantum.network.api.packet;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.util.Env;

import java.util.function.Supplier;

public abstract class ModPacketToClient<T extends ModPacketToClient<T>> extends ModPacket<T> implements ClientEndpoint {
    public ModPacketToClient() {
        super();
    }

    @Override
    public final boolean handle(Supplier<ModPacketContext> context) {
        PacketContext ctx = context.get();
        if (ctx.getDestination() == Env.CLIENT)
            ctx.queue(this::handle);
        return true;
    }

    protected abstract void handle();
}
