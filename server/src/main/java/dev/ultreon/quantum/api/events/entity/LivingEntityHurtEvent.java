package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.entity.damagesource.DamageSource;

public class LivingEntityHurtEvent extends LivingEntityEvent implements Cancelable {
    private DamageSource source;
    private float damage;
    private boolean canceled;

    public LivingEntityHurtEvent(LivingEntity entity, DamageSource source, float damage) {
        super(entity);
        this.source = source;
        this.damage = damage;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public void setSource(DamageSource source) {
        this.source = source;
    }

    public float getDamage() {
        return this.damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
