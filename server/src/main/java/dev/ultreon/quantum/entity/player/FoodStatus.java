package dev.ultreon.quantum.entity.player;

import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.item.food.FoodData;

public class FoodStatus {
    private final Player player;
    private int foodLevel;
    private float saturationLevel;
    private float exhaustion = 1.0f;
    private boolean isStarving;

    public FoodStatus(Player player, int foodLevel, int saturationLevel) {
        this.player = player;
        this.foodLevel = foodLevel;
        this.saturationLevel = saturationLevel;
    }

    public FoodStatus(Player player) {
        this(player, 20, 5);
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public void setSaturationLevel(int saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public boolean isFull() {
        return this.foodLevel >= 20;
    }

    public boolean isHungry() {
        return this.saturationLevel == 0;
    }

    public boolean isStarving() {
        return this.isStarving;
    }

    public boolean exhaust(float exhaustion) {
        this.exhaustion -= exhaustion;
        if (this.exhaustion < 0) {
            this.exhaustion = 20;
            decrementSaturation();
            return true;
        }
        return false;
    }

    private void decrementSaturation() {
        this.saturationLevel--;

        if (this.saturationLevel < 0) {
            this.saturationLevel = 0;
            this.hungrier();
        }
    }

    private void hungrier() {
        this.foodLevel--;

        if (this.foodLevel < 0) {
            this.foodLevel = 0;
            this.isStarving = true;
        }
    }

    public void eat(FoodData foodData) {
        this.foodLevel += foodData.getFood();
        this.saturationLevel += foodData.getSaturation();
        if (this.foodLevel >= 20) {
            this.foodLevel = 20;
        }

        this.exhaustion = 20;
        this.isStarving = false;
    }

    public void tick() {
        if (player.isWalking()) {
            exhaust(player.isRunning() ? 0.05f : 0.01f);
        }

        if (player.isSwimming()) {
            exhaust(0.1f);
        }

        if (player.jumping) exhaust(0.02f);

        if (this.isStarving) {
            player.hurt(1.0F, DamageSource.HUNGER);
        }
    }
}
