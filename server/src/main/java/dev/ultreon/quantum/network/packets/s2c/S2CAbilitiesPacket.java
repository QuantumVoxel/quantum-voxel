package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.player.PlayerAbilities;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.BitSet;

public record S2CAbilitiesPacket(boolean flying, boolean allowFlight, boolean instaMine, boolean invincible) implements AbilitiesPacket, Packet<InGameClientPacketHandler> {
    public S2CAbilitiesPacket(PlayerAbilities abilities) {
        this(abilities.flying, abilities.allowFlight, abilities.instaMine, abilities.invincible);
    }

    public static S2CAbilitiesPacket read(PacketIO buffer) {
        var bitSet = buffer.readBitSet();
        var flying = bitSet.get(0);
        var allowFlight = bitSet.get(1);
        var instaMine = bitSet.get(2);
        var invincible = bitSet.get(3);

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
        var bitSet = new BitSet();

        bitSet.set(0, this.flying);
        bitSet.set(1, this.allowFlight);
        bitSet.set(2, this.instaMine);
        bitSet.set(3, this.invincible);

        buffer.writeBitSet(bitSet);
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
}
