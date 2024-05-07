package dev.ultreon.quantum.component;

public abstract class GameComponent<T> {
    private final Class<? extends T> holder;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    protected GameComponent(T... typeGetter) {
        this.holder = (Class<? extends T>) typeGetter.getClass().getComponentType();
    }

    public Class<? extends T> getHolder() {
        return this.holder;
    }
}
