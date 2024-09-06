package dev.ultreon.quantapi.networking.api.packet;

import dev.ultreon.quantapi.networking.api.IPacketContext;
import dev.ultreon.quantum.util.Env;

import java.util.function.Supplier;

public abstract non-sealed class PacketToClient<T extends PacketToClient<T>> extends Packet<T> implements IClientEndpoint {
    public PacketToClient() {
        super();
    }

    @Override
    public final boolean handle(Supplier<IPacketContext> context) {
        IPacketContext ctx = context.get();
        if (ctx.getDestination() == Env.CLIENT)
            ctx.queue(this::handle);
        return true;
    }

    protected abstract void handle();
}
