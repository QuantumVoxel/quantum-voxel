package dev.ultreon.quantum.network.api.packet;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.function.Supplier;

public abstract class BiDirectionalModPacket<T extends BiDirectionalModPacket<T>> extends ModPacket<T> implements ClientEndpoint, ServerEndpoint {
    public BiDirectionalModPacket() {
        super();
    }

    @Override
    public final boolean handle(Supplier<ModPacketContext> context) {
        PacketContext ctx = context.get();
        switch (ctx.getDestination()) {
            case CLIENT:
                ctx.queue(this::handleClient);
                break;
            case SERVER:
                ctx.queue(() -> this.handleServer(ctx.getPlayer()));
                break;
            default:
                break;
        }
        return true;
    }

    protected abstract void handleClient();

    protected abstract void handleServer(ServerPlayer sender);
}
