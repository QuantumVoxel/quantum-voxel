package com.ultreon.quantum.world;

import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.util.Identifier;

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
