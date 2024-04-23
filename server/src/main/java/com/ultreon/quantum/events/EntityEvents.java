package com.ultreon.quantum.events;

import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.EntityType;
import com.ultreon.quantum.entity.LivingEntity;
import com.ultreon.quantum.entity.damagesource.DamageSource;
import com.ultreon.quantum.events.api.Event;
import com.ultreon.quantum.events.api.EventResult;
import com.ultreon.quantum.events.api.ValueEventResult;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public class EntityEvents {
    public static final Event<Damage> DAMAGE = Event.withValue();
    public static final Event<Death> DEATH = Event.withResult();
    public static final Event<Move> MOVE = Event.withValue();
    public static final Event<Removed> REMOVED = Event.create();

    @FunctionalInterface
    public interface Damage {
        ValueEventResult<Float> onEntityDamage(LivingEntity entity, DamageSource source, float damage);
    }

    @FunctionalInterface
    public interface Death {
        EventResult onEntityDeath(LivingEntity entity, DamageSource source);
    }

    @FunctionalInterface
    public interface Move {
        ValueEventResult<Vec3d> onEntityMove(Entity entity, double deltaX, double deltaY, double deltaZ);
    }

    @FunctionalInterface
    public interface Removed {
        void onEntityRemoved(Entity entity);
    }
}
