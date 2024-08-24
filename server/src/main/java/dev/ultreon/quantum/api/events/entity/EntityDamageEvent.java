package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.entity.Entity;

public class EntityDamageEvent extends EntityEvent implements Cancelable {
    private float damage;
    private boolean canceled;

    public EntityDamageEvent(Entity entity, float damage) {
        super(entity);
        this.damage = damage;
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
