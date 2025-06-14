package dev.ultreon.quantum.entity.player;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.item.food.FoodData;

public class FoodStatus {
    private final Player player;
    private int foodLevel;
    private float saturationLevel;
    private float exhaustion = 1.0f;
    private boolean isStarving;
    private float thirstLevel;

    public FoodStatus(Player player, int foodLevel, int saturationLevel, int thirstLevel) {
        this.player = player;
        this.foodLevel = foodLevel;
        this.saturationLevel = saturationLevel;
        this.thirstLevel = thirstLevel;
    }

    public FoodStatus(Player player) {
        this(player, 20, 5, 20);
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = Mth.clamp(foodLevel, 0, 20);
    }

    public void setSaturationLevel(int saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public void setThirstLevel(float thirstLevel) {
        this.thirstLevel = Mth.clamp(thirstLevel, 0, 20);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public float getThirstLevel() {
        return thirstLevel;
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

    public boolean isThirsty() {
        return this.thirstLevel == 0;
    }

    public boolean exhaust(float exhaustion) {
        this.exhaustion -= exhaustion;
        this.thirstLevel -= exhaustion / 20f;
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
        CommonConstants.LOGGER.debug("Eating food: " + foodData.getFood() + " saturation: " + foodData.getSaturation());
        this.foodLevel += foodData.getFood();
        this.saturationLevel += foodData.getSaturation();
        if (this.foodLevel >= 20) {
            this.foodLevel = 20;
        }

        this.exhaustion = 20;
        this.isStarving = false;

        CommonConstants.LOGGER.debug("Food status: " + this.foodLevel + " " + this.saturationLevel);
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

    public void reset() {
        this.foodLevel = 20;
        this.saturationLevel = 5;
        this.exhaustion = 1.0F;
        this.isStarving = false;
    }
}
