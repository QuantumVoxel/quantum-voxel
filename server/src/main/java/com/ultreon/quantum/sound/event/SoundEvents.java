package com.ultreon.quantum.sound.event;

import com.ultreon.quantum.registry.Registries;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.SoundEvent;

public class SoundEvents {
    public static final SoundEvent PLAYER_HURT = SoundEvents.register("entity.player.hurt", new SoundEvent(10.0f));
    public static final SoundEvent BUTTON_PRESS = SoundEvents.register("ui.button.press", new SoundEvent(10.0f));
    public static final SoundEvent BUTTON_RELEASE = SoundEvents.register("ui.button.release", new SoundEvent(10.0f));
    public static final SoundEvent SCREENSHOT = SoundEvents.register("ui.screenshot", new SoundEvent(10.0f));

    private static SoundEvent register(String name, SoundEvent event) {
        Registries.SOUND_EVENT.register(new Identifier(name), event);
        return event;
    }

    public static void nopInit() {

    }
}
