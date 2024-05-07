package dev.ultreon.quantum.world;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;

@SuppressWarnings("ClassCanBeRecord")
public class SoundEvent {
    private final float range;

    public SoundEvent(float range) {
        this.range = range;
    }

    public Identifier getId() {
        return Registries.SOUND_EVENT.getId(this);
    }

    public float getRange() {
        return this.range;
    }
}
