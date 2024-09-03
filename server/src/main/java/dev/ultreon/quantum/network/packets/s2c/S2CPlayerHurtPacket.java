package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public record S2CPlayerHurtPacket(float damage, DamageSource source) implements Packet<InGameClientPacketHandler> {

    public static S2CPlayerHurtPacket read(PacketIO buffer) {
        var damage = buffer.readFloat();
        var source = Registries.DAMAGE_SOURCE.get(buffer.readId());
        if (source == null) {
            source = DamageSource.NOTHING;
        }


        return new S2CPlayerHurtPacket(damage, source);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        NamespaceID type = this.source.getType();
        buffer.writeFloat(this.damage);
        buffer.writeId(type == null ? new NamespaceID("none") : type);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerHurt(this);
    }

    @Override
    public String toString() {
        return "S2CPlayerHurtPacket{" +
               "damage=" + this.damage +
               ", source=" + this.source +
               '}';
    }
}
