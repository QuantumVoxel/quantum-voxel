package dev.ultreon.quantum.client.render;

import org.jetbrains.annotations.NotNull;

public enum GraphicsSetting {
    LOW,
    NORMAL,
    HIGH,
    IMMERSIVE;

    public static GraphicsSetting of(Integer value) {
        if (value == null) return GraphicsSetting.NORMAL;
        switch (value) {
            case 0:
                return GraphicsSetting.LOW;
            case 2:
                return GraphicsSetting.HIGH;
            case 3:
                return GraphicsSetting.IMMERSIVE;
            default:
                return GraphicsSetting.NORMAL;
        }
    }

    public boolean isImmersive() {
        return compareTo(GraphicsSetting.IMMERSIVE) >= 0;
    }
}
