package dev.ultreon.quantum.client.gui.screens.options;

public enum Scale {
    AUTO(0),
    SMALL(1),
    MEDIUM(2),
    LARGE(3),
    EXTRA_LARGE(4),
    HUGE(5);

    private final int value;

    Scale(int value) {
        this.value = value;
    }

    public int get() {
        return this.value;
    }

    public static Scale of(int value) {
        for (Scale scale : Scale.values()) {
            if (scale.value == value) {
                return scale;
            }
        }
        return null;
    }
}
