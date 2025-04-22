package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.entity.player.PlayerAbilities;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.Objects;

public final class C2SAbilitiesPacket implements AbilitiesPacket, Packet<InGameServerPacketHandler> {
    private final boolean flying;

    public C2SAbilitiesPacket(boolean flying) {
        this.flying = flying;
    }

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

    @Override
    public boolean flying() {
        return flying;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SAbilitiesPacket) obj;
        return this.flying == that.flying;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flying);
    }

}
