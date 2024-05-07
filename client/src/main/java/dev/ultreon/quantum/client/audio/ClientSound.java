package dev.ultreon.quantum.client.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.SoundEvent;

public final class ClientSound {
    private final SoundEvent event;
    private Sound sound;

    public ClientSound(SoundEvent event) {
        this.event = event;
    }

    public void register() {
        this.sound = Gdx.audio.newSound(Gdx.files.internal(String.format("assets/%s/sounds/%s.mp3", this.getId().namespace(), this.getId().path().replace(".", "/"))));
    }

    public Identifier getId() {
        return Registries.SOUND_EVENT.getId(this.event);
    }

    public Sound getSound() {
        return this.sound;
    }
}
