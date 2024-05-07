package dev.ultreon.quantum.util;

import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;

import java.util.Random;

public class RandomValueSource implements ValueSource {
    private final RNG random;
    private final double min;
    private final double max;

    private RandomValueSource(RNG random, double min, double max) {
        this.random = random;
        this.min = min;
        this.max = max;
    }

    public static RandomValueSource of(Random random, double min, double max) {
        return new RandomValueSource(new JavaRNG(random), min, max);
    }

    @Override
    public double getValue() {
        return this.random.randrange(min, max);
    }
}
