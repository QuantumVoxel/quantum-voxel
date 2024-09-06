package dev.ultreon.quantum.world.rng;

import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class JavaRNG implements RNG {
    public static final @Nullable RNG GLOBAL = new JavaRNG();
    private final Random random;

    public JavaRNG() {
        this(new Random());
    }

    public JavaRNG(long seed) {
        this(new Random(seed));
    }

    public JavaRNG(Random random) {
        this.random = random;
    }

    @Override
    public int randint(int min, int max) {
        return this.random.nextInt(max - min + 1) + min;
    }

    @Override
    public boolean chance(int max) {
        return this.random.nextInt(max + 1) == 0;
    }

    @Override
    public boolean chance(float chance) {
        return this.random.nextFloat() <= chance;
    }

    @Override
    public float randrange(float min, float max) {
        return this.random.nextFloat(max - min) + min;
    }

    @Override
    public double randrange(double min, double max) {
        return this.random.nextDouble(max - min) + min;
    }

    @Override
    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    @Override
    public long nextLong() {
        return this.random.nextLong();
    }

    @Override
    public int nextInt(int bound) {
        return this.random.nextInt(bound);
    }

    @Override
    public float nextFloat() {
        return this.random.nextFloat();
    }

    @Override
    public int nextInt(int min, int max) {
        return this.random.nextInt(max - min) + min;
    }
}
