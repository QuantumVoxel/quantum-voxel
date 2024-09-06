package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.SoundEvent;

public record S2CPlaySoundPacket(NamespaceID sound, float volume) implements Packet<InGameClientPacketHandler> {

    public S2CPlaySoundPacket(SoundEvent sound, float volume) {
        this(sound.getId(), volume);
    }

    public static S2CPlaySoundPacket read(PacketIO buffer) {
        var sound = buffer.readId();
        var volume = buffer.readFloat();

        return new S2CPlaySoundPacket(sound, volume);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeId(this.sound);
        buffer.writeFloat(this.volume);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlaySound(this.sound, this.volume);
    }

    @Override
    public String toString() {
        return "S2CPlaySoundPacket{sound=" + this.sound + ", volume=" + this.volume + "}";
    }
}
