package dev.ultreon.quantum.world;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.Identifier;

@SuppressWarnings("ClassCanBeRecord")
public class SoundEvent {
    private final float range;
    private final boolean varyingPitch;

    public SoundEvent(float range) {
        this(range, false);
    }

    public SoundEvent(float range, boolean varyingPitch) {
        this.range = range;
        this.varyingPitch = varyingPitch;
    }

    public Identifier getId() {
        return Registries.SOUND_EVENT.getId(this);
    }

    public float getRange() {
        return this.range;
    }

    public boolean isVaryingPitch() {
        return this.varyingPitch;
    }
}
