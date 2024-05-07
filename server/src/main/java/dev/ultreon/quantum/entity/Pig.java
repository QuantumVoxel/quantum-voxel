package dev.ultreon.quantum.entity;

import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

public class Pig extends LivingEntity {
    private final Pose pose = Pose.IDLE;

    public Pig(EntityType<? extends Pig> entityType, World world) {
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

    public String getAnimation() {
        return "animation.model.new";
    }

    public Pose getPose() {
        return this.pose;
    }
}
