package dev.ultreon.quantum.sound;

import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoundType {
    public static final SoundType GRASS = new SoundType(SoundEvents.GRASS_STEP_1, SoundEvents.GRASS_STEP_2, SoundEvents.GRASS_STEP_3);
    public static final SoundType WOOD = new SoundType(SoundEvents.WOOD_STEP);
    public static final SoundType STONE = new SoundType(SoundEvents.STONE_STEP);
    public static final SoundType SAND = new SoundType(SoundEvents.SAND_STEP);
    public static final SoundType SNOW = new SoundType(SoundEvents.SNOW_STEP);

    private final List<SoundEvent> stepSounds;

    public SoundType(List<SoundEvent> stepSounds) {
        this.stepSounds = stepSounds;
    }

    public SoundType(SoundEvent... sounds) {
        this.stepSounds = List.of(sounds);
    }

    public @Nullable SoundEvent getStepSound(RNG rng) {
        if (this.stepSounds.isEmpty()) return null;
        return this.stepSounds.get(rng.nextInt(this.stepSounds.size()));
    }
}
