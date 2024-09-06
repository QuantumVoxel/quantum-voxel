package dev.ultreon.quantapi.networking.impl;

import dev.ultreon.quantapi.networking.api.IPacketContext;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Env;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ModPacketContext extends PacketContext implements IPacketContext {
    @NotNull
    private final ModNetChannel channel;

    public ModPacketContext(@NotNull ModNetChannel channel, @Nullable ServerPlayer player, @NotNull IConnection<?, ?> connection, @NotNull Env environment) {
        super(player, connection, environment);
        this.channel = channel;
    }

    public @NotNull ModNetChannel getChannel() {
        return this.channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ModPacketContext that = (ModPacketContext) o;
        return Objects.equals(this.getChannel(), that.getChannel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getChannel());
    }
}
