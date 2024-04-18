package com.ultreon.quantum.weather;

import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class Weather {
    public static final Weather SUNNY = new Weather();
    public static final Weather RAIN = new Weather();
    public static final Weather THUNDER = new Weather();

    public @Nullable Identifier getId() {
        return Registries.WEATHER.getId(this);
    }
}
