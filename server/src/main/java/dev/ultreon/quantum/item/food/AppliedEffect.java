package dev.ultreon.quantum.item.food;

import dev.ultreon.quantum.entity.LivingEntity;

import java.util.Locale;

public class AppliedEffect {

    private final StatusEffect effect;
    private final int ticks;
    private final int strength;
    private int ticksRemaining;

    public AppliedEffect(StatusEffect effect, int ticks, int strength) {
        this.effect = effect;
        this.ticks = ticks;
        this.strength = strength;
        this.ticksRemaining = ticks;
    }

    public void tick(LivingEntity entity) {
        if (ticksRemaining == ticks) {
            effect.onStart(entity);
        }

        ticksRemaining--;

        if (ticksRemaining <= 0) {
            effect.onEnd(entity);
        } else {
            effect.onTick(entity);
        }
    }

    public StatusEffect getEffect() {
        return effect;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public int getStartTicks() {
        return ticks;
    }

    public int getStrength() {
        return strength;
    }

    public boolean isExpired() {
        return ticksRemaining <= 0;
    }

    public boolean isFinished() {
        return ticksRemaining == ticks;
    }

    public boolean isActive() {
        return ticksRemaining > 0;
    }

    public String timeToString() {
        int seconds = ticksRemaining / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AppliedEffect) obj;
        return this.effect.equals(that.effect);
    }

    @Override
    public int hashCode() {
        return effect.hashCode();
    }

    @Override
    public String toString() {
        return "AppliedEffect{" +
                "effect=" + effect + ", " +
                "ticks=" + ticks + ", " +
                "strength=" + strength + ", " +
                "ticksRemaining=" + ticksRemaining +
                '}';
    }
}
