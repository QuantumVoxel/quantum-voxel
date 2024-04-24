package com.ultreon.quantum.entity;

import com.ultreon.quantum.sound.event.SoundEvents;
import com.ultreon.quantum.world.SoundEvent;
import com.ultreon.quantum.world.World;
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
