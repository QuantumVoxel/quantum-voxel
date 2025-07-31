package dev.ultreon.quantum.entity;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.api.events.entity.EntityEvent;
import dev.ultreon.quantum.api.events.entity.LivingEntityEvent;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.entity.ai.Navigator;
import dev.ultreon.quantum.entity.component.AirSupply;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.entity.player.Temperature;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.food.AppliedEffect;
import dev.ultreon.quantum.item.food.StatusEffect;
import dev.ultreon.quantum.network.packets.s2c.S2CRemoveEntityPacket;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.particles.ParticleTypes;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class LivingEntity extends Entity {
    public boolean walking;
    public boolean inverseAnim;
    public float walkAnim;
    protected float health;
    private float maxHealth = 20;
    protected boolean isDead = false;
    protected int damageImmunity = 0;

    public float jumpVel = 0.54F;
    public boolean jumping = false;
    public boolean invincible = false;
    protected float oldHealth;
    public float xHeadRot;
    protected float lastDamage;
    protected @Nullable DamageSource lastDamageSource;
    private int age;
    private final Set<AppliedEffect> appliedEffects = new LinkedHashSet<>();
    private boolean tagged;
    private Navigator navigator = this.set(Navigator.class, new Navigator(this));
    private final AirSupply airSupply = this.set(AirSupply.class, new AirSupply(10));
    protected double temperature;
    protected double temperatureGoal;
    protected @Nullable Entity lastAttacker = null;

    public LivingEntity(EntityType<? extends LivingEntity> entityType, WorldAccess world) {
        super(entityType, world);
    }

    public void createAiGoals() {

    }

    public float getHealth() {
        return this.health;
    }

    public void setHealth(float health) {
        this.health = Mth.clamp(health, 0, this.maxHealth);
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public float getJumpVel() {
        return this.jumpVel;
    }

    public void setJumpVel(float jumpVel) {
        this.jumpVel = jumpVel;
    }

    public boolean isInvincible() {
        return this.invincible;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public double getSpeed() {
        return this.attributes.get(Attribute.SPEED);
    }

    /**
     * <p>
     * This method represents the tick behavior for the {@link LivingEntity}.
     */
    @Override
    public void tick() {
        // If the entity is dead, do nothing
        if (this.isDead) return;

        age++;

        // Handle jumping logic
        if (this.jumping && (this.onGround || isAffectedByFluid())) {
            this.jump();
        }

        // Decrease damage immunity counter
        if (this.damageImmunity > 0) {
            this.damageImmunity--;
        }

        if (this.isBuried()) {
            this.hurt(0.5f, DamageSource.SUFFOCATION);
        }

        // Check if the entity is in the void and apply damage
        //noinspection deprecation
        if (this.isInVoid()) {
            this.hurtFromVoid();
        }

        // Check if the entity's health is zero to trigger death event
        if (this.health <= 0) {
            this.health = 0;

            // Trigger entity death event if not already dead and the event is not canceled
            if (!this.isDead && !EventSystem.postCancelable(new LivingEntityEvent.Death(this, lastDamageSource))) {
                this.isDead = true;
                this.onDeath(DamageSource.NOTHING);
            }
        }

        tickAirSupply();
        tickTemperature();

        // Call the superclass tick method
        super.tick();
    }

    @Override
    public Vec3d getLookVector(Vec3d direction) {
        this.yRot = Mth.clamp(this.yRot, -89.9F, 89.9F);
        direction.set(
                (float) (Math.cos(Math.toRadians(this.yRot)) * Math.sin(Math.toRadians(this.xHeadRot))),
                (float) (Math.cos(Math.toRadians(this.yRot)) * Math.cos(Math.toRadians(this.xHeadRot))),
                (float) Math.sin(Math.toRadians(this.yRot))
        );

        // Normalize the direction vector
        direction.nor();
        return direction;
    }

    protected void tickTemperature() {
        if (!(world instanceof ServerWorld)) return;
        ServerWorld serverWorld = (ServerWorld) world;
        temperatureGoal = Temperature.getTemperature(serverWorld, (int) x, (int) y, (int) z);

        final double delta = 0.1;
        final double acceleration = 24;
        if (temperature < temperatureGoal) { // Getting warmer.
            temperature = Math.min(temperatureGoal, temperature + (delta * ((temperatureGoal - temperature) / acceleration)));
        } else if (temperature > temperatureGoal) { // Getting colder.
            temperature = Math.max(temperatureGoal, temperature - (delta * ((temperature - temperatureGoal) / acceleration)));
        }
    }

    protected void tickAirSupply() {
        if (isBuried() && getBuriedBlock().getBlock() == Blocks.WATER)
            airSupply.setAir(airSupply.getAir() - 0.1f);

        if (getY() > 320) {
            double air = (getY() - 320) / 120f;
            airSupply.setAir(air >= 20 ? 0 : airSupply.getAir() - (float) air);
        }
    }

    @Override
    public void onPrepareSpawn(MapType spawnData) {
        super.onPrepareSpawn(spawnData);

        this.setHealth(this.maxHealth);
        this.setInitialTemperature();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();

        lastAttacker = null;
    }

    private void setInitialTemperature() {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            temperatureGoal = temperature = Temperature.getTemperature(serverWorld, (int) x, (int) y, (int) z);
        }
    }

    @Override
    @Deprecated
    public Vec3d getLookVector() {
        // Calculate the direction vector
        Vec3d direction = new Vec3d();

        this.yRot = Mth.clamp(this.yRot, -89.9F, 89.9F);
        direction.x = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.sin(Math.toRadians(this.xHeadRot)));
        direction.z = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.cos(Math.toRadians(this.xHeadRot)));
        direction.y = (float) Math.sin(Math.toRadians(this.yRot));

        // Normalize the direction vector
        direction.nor();
        return direction;
    }

    protected void hurtFromVoid() {
        this.hurt(Integer.MAX_VALUE, DamageSource.VOID);
    }

    public void jump() {
        if (this.isInWater()) return;
        this.velocityY = this.jumpVel;
    }

    /**
     * This method is called when the entity hits the ground.
     * If gravity is enabled and the entity is not in water, it calculates fall damage based on the fall distance.
     * If the calculated damage is greater than 0, it applies that damage to the entity.
     */
    @Override
    protected void hitGround() {
        if (!this.noGravity && !this.isInWater()) {
            int damage = (int) (this.fallDistance - 2.2f);
            if (damage > 0) {
                this.hurt(damage, DamageSource.FALLING);
            }
        }
    }

    /**
     * Inflicts damage on the entity based on the specified amount and source.
     *
     * @param damage the amount of damage to inflict
     * @param source the source of the damage
     */
    public final void hurt(float damage, DamageSource source) {
        // Trigger entity damage event
        EntityEvent.Damage event = new EntityEvent.Damage(this, source, damage);
        if (EventSystem.postCancelable(event)) return;
        damage = event.getDamage();

        // Check if the entity is already dead, has no health, or has temporary invincibility
        if (!event.isBypassingImmunity() && (this.isDead() || this.getHealth() <= 0 || (this.isInvincible() && !source.byPassInvincibility()) || (this.damageImmunity > 0)))
            return;

        // Check if custom onHurt behavior should be executed
        if (this.onHurt(damage, source)) return;

        // Play hurt sound if available
        SoundEvent hurtSound = this.getHurtSound();
        if (hurtSound != null) {
            this.world.playSound(hurtSound, this.x, this.y, this.z);
        }

        // Ensure damage is not negative
        if (event.isIgnoringLimits())
            damage = Math.max(damage, 0);

        // Update health and damage immunity
        this.oldHealth = this.health;
        this.health = Math.max(this.health - damage, 0);
        this.damageImmunity = 10;

        this.lastDamageSource = source;

        // Check if entity has died
        if (this.health <= 0) {
            this.health = 0;

            // Trigger entity death event and handle death
            if (!EventSystem.postCancelable(new LivingEntityEvent.Death(this, source))) {
                this.isDead = true;
                this.onDeath(source);
            }
        }
    }

    /**
     * Handles {@link #hurt(float, DamageSource)} for subclasses.
     *
     * @param damage the damage dealt.
     * @param source the source of the damage/
     * @return true to cancel the damage.
     */
    @ApiStatus.OverrideOnly
    public boolean onHurt(float damage, DamageSource source) {
        return false;
    }

    /**
     * Get the sound that should be played when the entity gets hurt.
     *
     * @return the hurt sound
     */
    @Nullable
    public SoundEvent getHurtSound() {
        return null;
    }

    /**
     * Called when the entity dies.
     */
    public void onDeath(DamageSource source) {
        // Play death sound if available
        SoundEvent deathSound = this.getDeathSound();
        if (deathSound != null) {
            this.world.playSound(deathSound, this.x, this.y, this.z);
        }

        this.onDropItems(source);

        this.world.spawnParticles(ParticleTypes.ENTITY_SMOKE, new Vec3d(this.x, this.y, this.z), new Vec3d(0, 0, 0), 20);

        this.removeDead();
    }

    protected void removeDead() {
        if (this.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            serverWorld.sendAllTracking((int) this.x, (int) this.y, (int) this.z, new S2CRemoveEntityPacket(this.getId()));
        }

        this.markRemoved();
    }

    public final void onDropItems(DamageSource source) {
        List<ItemStack> drops = getDrops();
        if (EventSystem.postCancelable(new LivingEntityEvent.DropItems(this, drops, source))) {
            drops.clear();
        }

        for (ItemStack drop : drops) {
            this.world.drop(drop, getPosition(tmp3D1));
        }
    }

    /**
     * Returns the drops for when the entity 'dies'.
     * The result can be modified, and doesn't affect constant loot.
     * <p>
     * For implementation: Make sure the returned values are mutable, and do not mess with the loot.
     * The list may be modified by third parties.
     */
    private List<ItemStack> getDrops() {
        return new ArrayList<>();
    }

    /**
     * Get the sound that should be played when the entity dies.
     *
     * @return the death sound
     */
    @Nullable
    public SoundEvent getDeathSound() {
        return null;
    }

    /**
     * Loads the state of the LivingEntity instance from the provided MapType data.
     * This includes health, maximum health, damage immunity, death status, jump velocity, jumping state, and invincibility.
     *
     * @param data the MapType object to load the entity's data from
     */
    @Override
    public void load(MapType data) {
        super.load(data);

        this.health = data.getFloat("health", this.health);
        this.maxHealth = data.getFloat("maxHealth", this.maxHealth);
        this.damageImmunity = data.getInt("damageImmunity", this.damageImmunity);
        this.isDead = data.getBoolean("isDead", this.isDead);
        this.jumpVel = data.getFloat("jumpVelocity", this.jumpVel);
        this.jumping = data.getBoolean("jumping", this.jumping);
        this.invincible = data.getBoolean("invincible", this.invincible);
    }

    /**
     * Saves the state of the LivingEntity instance to the provided MapType object.
     *
     * @param data the MapType object to save the entity's data to
     * @return the updated MapType object containing the saved state
     */
    @Override
    public MapType save(MapType data) {
        data = super.save(data);

        data.putFloat("health", this.health);
        data.putFloat("maxHealth", this.maxHealth);
        data.putInt("damageImmunity", this.damageImmunity);
        data.putBoolean("isDead", this.isDead);
        data.putFloat("jumpVelocity", this.jumpVel);
        data.putBoolean("jumping", this.jumping);
        data.putBoolean("jumping", this.invincible);

        return data;
    }

    /**
     * Check if the entity is dead.
     *
     * @return {@code true} if the entity is dead, {@code false} otherwise
     */
    public boolean isDead() {
        return this.isDead;
    }

    /**
     * Get the chunk position of the entity.
     *
     * @return the chunk position
     */
    public ChunkVec getChunkVec() {
        return this.getBlockVec().chunk();
    }

    /**
     * Instantly kill the entity.
     */
    public void kill() {
        // Set health to zero and mark as dead. Which basically is insta-kill.
        this.lastDamage = this.health;
        this.lastDamageSource = DamageSource.KILL;
        this.health = 0;
        this.isDead = true;

        // Trigger entity death event and handle death
        this.onDeath(DamageSource.KILL);
    }

    public boolean isWalking() {
        return this.walking;
    }

    public int getAge() {
        return age;
    }

    public @Nullable DamageSource getLastDamageSource() {
        return lastDamageSource;
    }

    public void applyEffect(AppliedEffect appliedEffect) {
        this.appliedEffects.add(appliedEffect);
    }

    public void moveTowards(double x, double y, double z, double speed) {
        this.x += (x - this.x) / speed;
        this.y += (y - this.y) / speed;
        this.z += (z - this.z) / speed;
    }

    public boolean isBuried() {
        BlockState buriedBlock = getBuriedBlock();
        return buriedBlock.hasCollider() && buriedBlock.getBlock() != Blocks.BARRIER;
    }

    public BlockState getBuriedBlock() {
        Vec3d add = getPosition(tmp3D1).add(0, getEyeHeight(), 0);
        return world.get((int) Math.floor(add.x), (int) Math.floor(add.y), (int) Math.floor(add.z));
    }

    public float getEyeHeight() {
        return getType().getEyeHeight();
    }

    public float getAir() {
        return airSupply.getAir();
    }

    public int getMaxAir() {
        return airSupply.getMaxAir();
    }

    /**
     * Retrieves the current temperature in Celsius.
     *
     * @return the current temperature as a floating-point number.
     */
    public double getTemperature() {
        return temperature;
    }

    public @Nullable Entity getLastAttacker() {
        return lastAttacker;
    }

    public void setLastAttacker(Entity entity) {
        this.lastAttacker = entity;
    }

    public void applyEffect(StatusEffect effect, int duration, int amplifier) {
        this.applyEffect(new AppliedEffect(effect, duration, amplifier));
    }

    public void removeEffect(StatusEffect effect) {
        this.appliedEffects.removeIf(appliedEffect -> appliedEffect.getEffect() == effect);
    }

    public Set<AppliedEffect> getAppliedEffects() {
        return appliedEffects;
    }

    public boolean isTagged() {
        return tagged;
    }

    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public boolean isAlive() {
        return !isDead && getHealth() > 0;
    }
}