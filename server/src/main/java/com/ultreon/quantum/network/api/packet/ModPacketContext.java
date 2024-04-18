package com.ultreon.quantum.network.api.packet;

import com.ultreon.quantum.network.system.IConnection;
import com.ultreon.quantum.network.NetworkChannel;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.server.player.ServerPlayer;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ModPacketContext extends PacketContext {
    @NotNull
    private final NetworkChannel channel;

    public ModPacketContext(@NotNull NetworkChannel channel, @Nullable ServerPlayer player, @NotNull IConnection connection, @NotNull EnvType environment) {
        super(player, connection, environment);
        this.channel = channel;
    }

    public @NotNull NetworkChannel getChannel() {
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
