package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.player.PlayerAbilities;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.BitSet;
import java.util.Objects;

public final class S2CAbilitiesPacket implements AbilitiesPacket, Packet<InGameClientPacketHandler> {
    private final boolean flying;
    private final boolean allowFlight;
    private final boolean instaMine;
    private final boolean invincible;

    public S2CAbilitiesPacket(boolean flying, boolean allowFlight, boolean instaMine, boolean invincible) {
        this.flying = flying;
        this.allowFlight = allowFlight;
        this.instaMine = instaMine;
        this.invincible = invincible;
    }

    public S2CAbilitiesPacket(PlayerAbilities abilities) {
        this(abilities.flying, abilities.allowFlight, abilities.instaMine, abilities.invincible);
    }

    public static S2CAbilitiesPacket read(PacketIO buffer) {
        int bitSet = buffer.readByte();
        boolean flying = (bitSet & 1) == 1;
        boolean allowFlight = (bitSet & 2) == 2;
        boolean instaMine = (bitSet & 4) == 4;
        boolean invincible = (bitSet & 8) == 8;

        return new S2CAbilitiesPacket(flying, allowFlight, instaMine, invincible);
    }

    @Override
    public boolean flying() {
        return this.flying;
    }

    @Override
    public boolean allowFlight() {
        return this.allowFlight;
    }

    @Override
    public boolean isInstaMine() {
        return this.instaMine;
    }

    @Override
    public boolean isInvincible() {
        return this.invincible;
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(
                (this.flying ? 1 : 0) |
                (this.allowFlight ? 2 : 0) |
                (this.instaMine ? 4 : 0) |
                (this.invincible ? 8 : 0)
        );
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onAbilities(this);
    }

    @Override
    public String toString() {
        return "S2CAbilitiesPacket{" +
               "flying=" + flying +
               ", allowFlight=" + allowFlight +
               ", instaMine=" + instaMine +
               ", invincible=" + invincible +
               '}';
    }

    public boolean instaMine() {
        return instaMine;
    }

    public boolean invincible() {
        return invincible;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        S2CAbilitiesPacket that = (S2CAbilitiesPacket) obj;
        return this.flying == that.flying &&
               this.allowFlight == that.allowFlight &&
               this.instaMine == that.instaMine &&
               this.invincible == that.invincible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flying, allowFlight, instaMine, invincible);
    }

}
