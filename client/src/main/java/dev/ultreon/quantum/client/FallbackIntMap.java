package dev.ultreon.quantum.client;

import com.badlogic.gdx.utils.IntMap;

import java.util.function.BooleanSupplier;

public class FallbackIntMap<T> extends IntMap<T> {
    private IntMap<T> fallback;
    private BooleanSupplier forceFallbackCondition = () -> false;

    public FallbackIntMap() {
    }

    public FallbackIntMap(int initialCapacity) {
        super(initialCapacity);
    }

    public FallbackIntMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public FallbackIntMap(IntMap<? extends T> map) {
        super(map);
    }

    @Override
    public T get(int key) {
        if (forceFallbackCondition.getAsBoolean() && fallback != null) {
            return fallback.get(key);
        }

        if (!containsKey(key) && fallback != null) {
            return fallback.get(key);
        }

        return super.get(key);
    }

    @Override
    public T get(int key, T defaultValue) {
        if (forceFallbackCondition.getAsBoolean() && fallback != null) {
            return fallback.get(key, defaultValue);
        }

        if (!containsKey(key) && fallback != null) {
            return fallback.get(key, defaultValue);
        }

        return super.get(key, defaultValue);
    }

    public void setForceFallbackCondition(BooleanSupplier forceFallbackCondition) {
        this.forceFallbackCondition = forceFallbackCondition;
    }

    public IntMap<T> getFallback() {
        return fallback;
    }

    public void setFallback(IntMap<T> fallback) {
        this.fallback = fallback;
    }
}
