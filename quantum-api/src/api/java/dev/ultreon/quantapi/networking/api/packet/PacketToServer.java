package dev.ultreon.quantapi.networking.api.packet;

import dev.ultreon.quantapi.networking.api.IPacketContext;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class PacketToServer<T extends PacketToServer<T>> extends Packet<T> implements IServerEndpoint {
    @Override
    public final boolean handle(Supplier<IPacketContext> context) {
        IPacketContext ctx = context.get();
        if (ctx.getDestination() == Env.SERVER)
            ctx.queue(() -> this.handle(Objects.requireNonNull(ctx.getPlayer(), "Server player was not found while on server side")));
        return true;
    }

    protected abstract void handle(@NotNull ServerPlayer sender);
}
