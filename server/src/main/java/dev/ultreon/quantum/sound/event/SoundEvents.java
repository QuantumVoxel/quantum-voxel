package dev.ultreon.quantum.sound.event;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.SoundEvent;

public class SoundEvents {
    public static final SoundEvent PLAYER_HURT = SoundEvents.register("entity.player.hurt", new SoundEvent(10.0f));
    public static final SoundEvent BUTTON_PRESS = SoundEvents.register("ui.button.press", new SoundEvent(10.0f));
    public static final SoundEvent BUTTON_RELEASE = SoundEvents.register("ui.button.release", new SoundEvent(10.0f));
    public static final SoundEvent SCREENSHOT = SoundEvents.register("ui.screenshot", new SoundEvent(10.0f));
    public static final SoundEvent WALK = SoundEvents.register("entity.player.walk", new SoundEvent(10.0f, true));

    private static SoundEvent register(String name, SoundEvent event) {
        Registries.SOUND_EVENT.register(new NamespaceID(name), event);
        return event;
    }

    public static void init() {

    }
}
