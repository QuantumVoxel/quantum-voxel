package dev.ultreon.quantum.util;

import com.badlogic.gdx.math.RandomXS128;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;

public class RandomValueSource implements ValueSource {
    private final RNG random;
    private final double min;
    private final double max;

    private RandomValueSource(RNG random, double min, double max) {
        this.random = random;
        this.min = min;
        this.max = max;
    }

    public static RandomValueSource of(RandomXS128 random, double min, double max) {
        return new RandomValueSource(new JavaRNG(random), min, max);
    }

    @Override
    public double getValue() {
        return this.random.randrange(min, max);
    }
}
