package dev.ultreon.quantum.events;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.events.api.EventResult;
import dev.ultreon.quantum.events.api.ValueEventResult;
import dev.ultreon.quantum.util.Vec3d;

public class EntityEvents {
    public static final Event<Damage> DAMAGE = Event.withValue(listeners -> (entity, source, damage) -> {
        ValueEventResult<Float> result = ValueEventResult.pass();
        for (Damage listener : listeners) {
            ValueEventResult<Float> floatValueEventResult = listener.onEntityDamage(entity, source, damage);
            if (floatValueEventResult.isCanceled()) return floatValueEventResult;
            if (floatValueEventResult.isInterrupted()) result = floatValueEventResult;
        }
        return result;
    });
    public static final Event<Death> DEATH = Event.withResult(listeners -> (entity, source) -> {
        EventResult result = EventResult.pass();
        for (Death listener : listeners) {
            EventResult eventResult = listener.onEntityDeath(entity, source);
            if (eventResult.isCanceled()) return eventResult;
            if (eventResult.isInterrupted()) result = eventResult;
        }
        return result;
    });
    public static final Event<Move> MOVE = Event.withValue(listeners -> (entity, deltaX, deltaY, deltaZ) -> {
        ValueEventResult<Vec3d> result = ValueEventResult.pass();
        for (Move listener : listeners) {
            ValueEventResult<Vec3d> vec3dValueEventResult = listener.onEntityMove(entity, deltaX, deltaY, deltaZ);
            if (vec3dValueEventResult.isCanceled()) return vec3dValueEventResult;
            if (vec3dValueEventResult.isInterrupted()) result = vec3dValueEventResult;
        }
        return result;
    });
    public static final Event<Removed> REMOVED = Event.create(listeners -> entity -> {
        for (Removed listener : listeners) {
            listener.onEntityRemoved(entity);
        }
    });

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
