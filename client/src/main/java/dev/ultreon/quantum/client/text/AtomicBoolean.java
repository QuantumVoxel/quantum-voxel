package dev.ultreon.quantum.client.text;

public class AtomicBoolean {
    private boolean value;

    public AtomicBoolean(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return this.value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}
