package dev.ultreon.quantum.client.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.world.SoundEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class ClientSoundRegistry {
    private Map<Identifier, Sound> soundMap = Collections.emptyMap();

    public ClientSoundRegistry() {

    }

    @ApiStatus.Internal
    public void registerSounds() {
        Registry<SoundEvent> soundEvents = Registries.SOUND_EVENT;
        Map<Identifier, Sound> soundMap = new HashMap<>();
        for (Map.Entry<RegistryKey<SoundEvent>, SoundEvent> entry : soundEvents.entries()) {
            Identifier key = entry.getKey().element();
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(String.format("assets/%s/sounds/%s.ogg", key.namespace(), key.path().replaceAll("\\.", "/"))));

            soundMap.put(key, sound);
        }

        this.soundMap = soundMap;
    }

    public Sound getSound(Identifier id) {
        return this.soundMap.get(id);
    }

    public void reload() {
        Collection<Sound> old = List.copyOf(soundMap.values());
        this.soundMap.clear();
        old.forEach(Sound::dispose);

        this.registerSounds();
    }
}
