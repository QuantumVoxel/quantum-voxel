package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.entity.damagesource.DamageSource;

public class LivingEntityDeathEvent extends LivingEntityEvent {
    private DamageSource source;

    public LivingEntityDeathEvent(LivingEntity entity, DamageSource source) {
        super(entity);
        this.source = source;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public void setSource(DamageSource source) {
        this.source = source;
    }
}
