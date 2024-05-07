package dev.ultreon.quantum.entity;

import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

public class Something extends LivingEntity {
    public Something(EntityType<? extends Something> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public @Nullable SoundEvent getHurtSound() {
        return SoundEvents.SCREENSHOT;
    }

    @Override
    public float getMaxHealth() {
        return 10;
    }
}
