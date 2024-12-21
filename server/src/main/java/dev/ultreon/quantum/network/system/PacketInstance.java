package dev.ultreon.quantum.network.system;

import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PacketInstance<T extends @NotNull Packet<?>>(
        @NotNull T packet,
        @Nullable PacketListener listener
) {
}
