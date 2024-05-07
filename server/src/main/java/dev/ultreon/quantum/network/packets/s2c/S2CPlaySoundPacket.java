package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.Identifier;

public class S2CPlaySoundPacket extends Packet<InGameClientPacketHandler> {
    private final Identifier sound;
    private final float volume;

    public S2CPlaySoundPacket(Identifier sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    public S2CPlaySoundPacket(PacketIO buffer) {
        this.sound = buffer.readId();
        this.volume = buffer.readFloat();
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
}
