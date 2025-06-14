package dev.ultreon.quantum.world.rng;

public interface RNG {
    int randint(int min, int max);
    boolean chance(int max);
    boolean chance(float chance);
    float randrange(float min, float max);
    double randrange(double min, double max);
    void setSeed(long seed);
    long nextLong();

    int nextInt(int bound);

    float nextFloat();

    int nextInt(int min, int max);
}
