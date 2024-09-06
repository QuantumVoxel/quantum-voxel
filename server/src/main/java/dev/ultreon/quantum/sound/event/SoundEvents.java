package dev.ultreon.quantum.sound.event;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.SoundEvent;

public class SoundEvents {
    public static final SoundEvent PLAYER_HURT = SoundEvents.register("entity.player.hurt", new SoundEvent(10.0f, true));
    public static final SoundEvent SWORD_HIT = SoundEvents.register("entity.player.sword_hit", new SoundEvent(10.0f, true));
    public static final SoundEvent ENTITY_HIT = SoundEvents.register("entity.hit", new SoundEvent(10.0f, true));
    public static final SoundEvent BUTTON_PRESS = SoundEvents.register("ui.button.press", new SoundEvent(10.0f));
    public static final SoundEvent BUTTON_RELEASE = SoundEvents.register("ui.button.release", new SoundEvent(10.0f));
    public static final SoundEvent SCREENSHOT = SoundEvents.register("ui.screenshot", new SoundEvent(10.0f));
    public static final SoundEvent GRASS_STEP_1 = SoundEvents.register("step.grass.1", new SoundEvent(10.0f, true));
    public static final SoundEvent GRASS_STEP_2 = SoundEvents.register("step.grass.2", new SoundEvent(10.0f, true));
    public static final SoundEvent GRASS_STEP_3 = SoundEvents.register("step.grass.3", new SoundEvent(10.0f, true));
    public static final SoundEvent WOOD_STEP = SoundEvents.register("step.wood", new SoundEvent(10.0f, true));
    public static final SoundEvent STONE_STEP = SoundEvents.register("step.stone.1", new SoundEvent(10.0f, true));
    public static final SoundEvent SAND_STEP = SoundEvents.register("step.sand", new SoundEvent(10.0f, true));
    public static final SoundEvent SNOW_STEP = SoundEvents.register("step.snow", new SoundEvent(10.0f, true));

    private static SoundEvent register(String name, SoundEvent event) {
        Registries.SOUND_EVENT.register(new NamespaceID(name), event);
        return event;
    }

    public static void init() {

    }
}
