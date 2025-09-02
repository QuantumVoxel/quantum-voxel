package dev.ultreon.quantum.sound;

import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The SoundType class encapsulates different types of sound events associated
 * with specific materials or actions in the game, such as walking on grass or wood.
 */
public class SoundType {
    public static final SoundType GRASS = new SoundType(SoundEvents.GRASS_STEP_1, SoundEvents.GRASS_STEP_2, SoundEvents.GRASS_STEP_3);
    public static final SoundType WOOD = new SoundType(SoundEvents.WOOD_STEP);
    public static final SoundType STONE = new SoundType(SoundEvents.STONE_STEP);
    public static final SoundType SAND = new SoundType(SoundEvents.SAND_STEP);
    public static final SoundType SNOW = new SoundType(SoundEvents.SNOW_STEP);

    private final List<SoundEvent> stepSounds;

    /**
     * Constructs a new SoundType with a list of step sound events.
     *
     * @param stepSounds A list of SoundEvent objects representing the step sounds for this SoundType.
     */
    public SoundType(List<SoundEvent> stepSounds) {
        this.stepSounds = stepSounds;
    }

    /**
     * Constructs a new SoundType instance with step sounds provided as a variable number of
     * SoundEvent arguments.
     *
     * @param sounds A variable number of SoundEvent objects representing different step sounds.
     */
    public SoundType(SoundEvent... sounds) {
        this.stepSounds = List.of(sounds);
    }

    /**
     * Retrieves a random step sound from the list of step sounds available for this SoundType.
     *
     * @param rng An RNG (random number generator) instance used to randomly select a step sound.
     * @return A randomly selected SoundEvent representing a step sound, or null if no step sounds are available.
     */
    public @Nullable SoundEvent getStepSound(RNG rng) {
        if (this.stepSounds.isEmpty()) return null;
        return this.stepSounds.get(rng.nextInt(this.stepSounds.size()));
    }
}
