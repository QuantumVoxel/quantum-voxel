package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.player.PlayerAbilities;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;

import java.util.BitSet;

public class C2SAbilitiesPacket extends Packet<InGameServerPacketHandler> implements AbilitiesPacket {
    private final boolean flying;
    private final BitSet bitSet;

    public C2SAbilitiesPacket(PlayerAbilities abilities) {
        this.flying = abilities.flying;
        this.bitSet = new BitSet();
        this.bitSet.set(0, this.flying);
    }

    public C2SAbilitiesPacket(PacketIO buffer) {
        this.bitSet = buffer.readBitSet();
        this.flying = this.bitSet.get(0);
    }

    @Override
    public boolean isFlying() {
        return this.flying;
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
        buffer.writeBitSet(this.bitSet);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onAbilities(this);
    }
}
