package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.entity.player.PlayerAbilities;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

public record C2SAbilitiesPacket(boolean flying) implements AbilitiesPacket, Packet<InGameServerPacketHandler> {

    public C2SAbilitiesPacket(PlayerAbilities abilities) {
        this(abilities.flying);
    }

    public static C2SAbilitiesPacket read(PacketIO buffer) {
        return new C2SAbilitiesPacket(buffer.readBoolean());
    }

    @Override
    public boolean allowFlight() {
        return false;
    }

    @Override
    public boolean isInstaMine() {
        return false;
    }

    @Override
    public boolean isInvincible() {
        return false;
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeBoolean(this.flying);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onAbilities(this);
    }

    @Override
    public String toString() {
        return "C2SAbilitiesPacket{flying=" + this.flying + '}';
    }
}
