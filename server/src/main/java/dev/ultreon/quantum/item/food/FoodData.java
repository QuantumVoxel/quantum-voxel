package dev.ultreon.quantum.item.food;

import dev.ultreon.quantum.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class FoodData {
    private final int food;
    private final float saturation;

    public FoodData(int food, float saturation) {
        this.food = food;
        this.saturation = saturation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getFood() {
        return food;
    }

    public float getSaturation() {
        return saturation;
    }

    public abstract void onEaten(LivingEntity entity);

    public static final class Builder {
        private final List<StatusEffectFactory> effects = new ArrayList<>();
        private int food;
        private float saturation;

        private Builder() {

        }

        public FoodData build() {
            return new BuiltFoodData();
        };

        public Builder food(int food) {
            this.food = food;
            return this;
        }

        public Builder saturation(float saturation) {
            this.saturation = saturation;
            return this;
        }

        private class BuiltFoodData extends FoodData {
            public BuiltFoodData() {
                super(Builder.this.food, Builder.this.saturation);
            }

            @Override
            public void onEaten(LivingEntity entity) {
                for (StatusEffectFactory effect : effects) {
                    entity.applyEffect(effect.create());
                }
            }
        }
    }

    @FunctionalInterface
    public interface StatusEffectFactory {
        AppliedEffect create();
    }
}
