package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.player.PlayerAbilities;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;

import java.util.BitSet;

public class S2CAbilitiesPacket extends Packet<InGameClientPacketHandler> implements AbilitiesPacket {
    private final boolean flying;
    private final boolean allowFlight;
    private final boolean instaMine;
    private final boolean invincible;
    private final BitSet bitSet;

    public S2CAbilitiesPacket(PlayerAbilities abilities) {
        this.flying = abilities.flying;
        this.allowFlight = abilities.allowFlight;
        this.invincible = abilities.invincible;
        this.instaMine = abilities.instaMine;
        this.bitSet = new BitSet();
        this.bitSet.set(0, this.flying);
        this.bitSet.set(1, this.allowFlight);
        this.bitSet.set(2, this.instaMine);
        this.bitSet.set(3, this.invincible);
    }

    public S2CAbilitiesPacket(PacketIO buffer) {
        this.bitSet = buffer.readBitSet();
        this.flying = this.bitSet.get(0);
        this.allowFlight = this.bitSet.get(1);
        this.instaMine = this.bitSet.get(2);
        this.invincible = this.bitSet.get(3);
    }

    @Override
    public boolean isFlying() {
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
        buffer.writeBitSet(this.bitSet);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onAbilities(this);
    }
}
