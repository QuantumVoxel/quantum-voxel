package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class S2CRemovePlayerPacket extends Packet<InGameClientPacketHandler> {
    private final UUID uuid;

    public S2CRemovePlayerPacket(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public S2CRemovePlayerPacket(PacketIO buffer) {
        this.uuid = buffer.readUuid();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUuid(this.uuid);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemovePlayer(this.uuid);
    }
}
