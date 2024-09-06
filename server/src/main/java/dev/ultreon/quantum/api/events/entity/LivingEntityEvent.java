package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.entity.LivingEntity;

public abstract class LivingEntityEvent extends EntityEvent {
    public LivingEntityEvent(LivingEntity entity) {
        super(entity);
    }

    public LivingEntity getLivingEntity() {
        return (LivingEntity) getEntity();
    }
}
