package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;

public class S2CPlayerHurtPacket extends Packet<InGameClientPacketHandler> {
    private final float damage;
    private final DamageSource source;

    public S2CPlayerHurtPacket(float damage, DamageSource source) {
        this.damage = damage;
        this.source = source;
    }

    public S2CPlayerHurtPacket(PacketIO buffer) {
        this.damage = buffer.readFloat();
        var source = Registries.DAMAGE_SOURCE.get(buffer.readId());
        if (source == null) {
            source = DamageSource.NOTHING;
        }
        this.source = source;
    }

    @Override
    public void toBytes(PacketIO buffer) {
        Identifier type = this.source.getType();
        buffer.writeFloat(this.damage);
        buffer.writeId(type == null ? new Identifier("none") : type);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerHurt(this);
    }

    public float getDamage() {
        return this.damage;
    }

    public DamageSource getSource() {
        return this.source;
    }
}
