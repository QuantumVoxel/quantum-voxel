package dev.ultreon.quantum.entity.player;

import com.google.common.base.Preconditions;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.entity.Attribute;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.events.ItemEvents;
import dev.ultreon.quantum.events.MenuEvents;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.CrateMenu;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.menu.MenuTypes;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public abstract class Player extends LivingEntity {
    public int selected;
    public Inventory inventory;
    public int regenFlashTimer = -1;
    private boolean running = false;
    private float walkingSpeed = .09F;
    private float flyingSpeed = 0.3F;
    public float runModifier = 8.0F;
    public float crouchModifier = 0.2F;
    public final PlayerAbilities abilities = new PlayerAbilities();
    private boolean crouching = false;
    @Nullable
    protected ContainerMenu openMenu;
    private ItemStack cursor = new ItemStack();
    private final String name;
    private GameMode gamemode = GameMode.SURVIVAL;
    private final FoodStatus foodStatus = new FoodStatus(this);
    private float reach = 6.0F;

    protected Player(EntityType<? extends Player> entityType, WorldAccess world, String name) {
        super(entityType, world);

        this.inventory = new Inventory(MenuTypes.INVENTORY, world, this, null);
        this.name = name;
        this.inventory.build();

        this.inventory.addWatcher(this);
    }

    @Override
    public double getSpeed() {
        return this.isFlying() ? this.getFlyingSpeed() : this.getWalkingSpeed();
    }

    @Override
    protected void setupAttributes() {
        this.attributes.setBase(Attribute.SPEED, this.getWalkingSpeed());
        this.attributes.setBase(Attribute.BLOCK_REACH, 6);
        this.attributes.setBase(Attribute.ENTITY_REACH, 6);
    }

    @Override
    public boolean isInvincible() {
        return this.abilities.invincible;
    }

    @Override
    public void setInvincible(boolean invincible) {
        this.abilities.invincible = invincible;
        this.sendAbilities();
    }

    public void selectBlock(int i) {
        int toSelect = i % 9;
        if (toSelect < 0) toSelect += 9;
        this.selected = toSelect;
    }

    public ItemStack getSelectedItem() {
        if (this.selected < 0) this.selected = 0;
        return this.selected >= 9 ? ItemStack.empty() : this.inventory.getHotbarSlot(this.selected).getItem();
    }

    @Override
    public void tick() {
        this.x = Mth.clamp(this.x, -30000000, 30000000);
        this.z = Mth.clamp(this.z, -30000000, 30000000);

        if (health > oldHealth) regenFlashTimer = 4;
        if (regenFlashTimer >= 0) regenFlashTimer--;

        super.tick();

        this.foodStatus.tick();

        if (this.isHurt() && !isDead) {
            this.regenerate();
        }
    }

    private void regenerate() {
        if (foodStatus.exhaust(0.1f)) {
            this.oldHealth = this.health;
            this.health = Mth.clamp(this.health + 1, 0, this.getMaxHealth());
        }
    }

    private boolean isHurt() {
        return this.health < this.getMaxHealth();
    }

    @Override
    protected void hurtFromVoid() {
        super.hurtFromVoid();

        this.onVoidDamage();
    }

    protected void onVoidDamage() {

    }

    @Override
    public void jump() {
        if (isAffectedByFluid()) {
            this.swimUp();
            return;
        }

        super.jump();
    }

    @Override
    public boolean isAffectedByFluid() {
        return !(this.abilities.flying || this.noClip) && super.isAffectedByFluid();
    }

    @Override
    public void setRotation(@NotNull Vec2f position) {
        super.setRotation(position);
        this.xHeadRot = position.x;
    }

    @Override
    public void setPosition(@NotNull Vec3d position) {
        position.x = Mth.clamp(position.x, -30000000, 30000000);
        position.z = Mth.clamp(position.z, -30000000, 30000000);
        super.setPosition(position);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        x = Mth.clamp(x, -30000000, 30000000);
        z = Mth.clamp(z, -30000000, 30000000);
        super.setPosition(x, y, z);
    }

    @Override
    public @NotNull TextObject getDisplayName() {
        return TextObject.literal(this.name);
    }

    public void rotateHead(float x, float y) {
        this.xHeadRot += x;
        this.yRot = Mth.clamp(this.yRot + y, -90, 90);
        this.xRot = Mth.clamp(this.xRot, this.xHeadRot - 50, this.xHeadRot + 50);
    }

    public float getEyeHeight() {
        return this.crouching ? 1.15F : 1.63F;
    }

    public boolean isRunning() {
        return this.running && (this.ox != this.x || this.oz != this.z || this.oy != this.y) & isWalking();
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public float getWalkingSpeed() {
        return this.isRunning() ? this.walkingSpeed * this.runModifier : this.walkingSpeed;
    }

    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    public float getFlyingSpeed() {
        return isRunning() ? this.flyingSpeed * this.runModifier : this.flyingSpeed;
    }

    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    public boolean isFlying() {
        return this.abilities.flying;
    }

    public void setFlying(boolean flying) {
        this.noGravity = this.abilities.flying = flying;
        this.sendAbilities();
    }

    public boolean isCrouching() {
        return this.crouching;
    }

    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    public boolean isSpectator() {
        return this.gamemode == GameMode.SPECTATOR;
    }

    @Deprecated
    public boolean isSpectating() {
        return this.isSpectator();
    }

    @Deprecated
    public void setSpectating(boolean spectating) {
        this.setGameMode(spectating ? GameMode.SPECTATOR : GameMode.SURVIVAL);
    }

    @Override
    public SoundEvent getHurtSound() {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected final void removeDead() {
        // Don't remove dead players, removing them causes the player to get completely unresponsive
        //    and will break client <-> server connection. DO NOT CALL SUPER HERE!
    }

    public Hit rayCast() {
        Ray ray1 = new Ray(this.getPosition().add(0, this.getEyeHeight(), 0), this.getLookVector());
        return this.world.rayCast(ray1, this, this.getReach(), CommonConstants.VEC3D_0_C);
    }

    @Override
    public void load(@NotNull MapType data) {
        super.load(data);

        this.selected = data.getByte("selectedItem", (byte) this.selected);
        this.crouching = data.getBoolean("crouching", this.crouching);
        this.running = data.getBoolean("running", this.running);
        this.walkingSpeed = data.getFloat("walkingSpeed", this.walkingSpeed);
        this.flyingSpeed = data.getFloat("flyingSpeed", this.flyingSpeed);
        this.crouchModifier = data.getFloat("crouchingModifier", this.crouchModifier);
        this.runModifier = data.getFloat("runModifier", this.runModifier);
        this.gamemode = Objects.requireNonNullElse(GameMode.byOrdinal(data.getByte("gamemode", (byte) 0)), GameMode.SURVIVAL);
        this.abilities.load(data.getMap("Abilities"));
    }

    @Override
    public @NotNull MapType save(@NotNull MapType data) {
        data = super.save(data);

        data.putByte("selectedItem", this.selected);
        data.putBoolean("crouching", this.crouching);
        data.putBoolean("running", this.running);
        data.putFloat("walkingSpeed", this.walkingSpeed);
        data.putFloat("flyingSpeed", this.flyingSpeed);
        data.putFloat("crouchingModifier", this.crouchModifier);
        data.putFloat("runModifier", this.runModifier);
        data.putByte("gamemode", (byte) this.gamemode.ordinal());
        data.put("Abilities", this.abilities.save(new MapType()));

        return data;
    }

    /**
     * Play a sound event and volume.
     *
     * @param sound The sound event to be played. Can be null.
     * @param volume The volume at which the sound should be played.
     */
    public void playSound(@Nullable SoundEvent sound, float volume) {

    }

    public boolean isAllowFlight() {
        return this.abilities.allowFlight;
    }

    public void setAllowFlight(boolean allowFlight) {
        this.abilities.allowFlight = allowFlight;
        this.sendAbilities();
    }

    protected abstract void sendAbilities();

    protected void onAbilities(AbilitiesPacket packet) {
        this.noGravity = packet.isFlying();
    }

    public @Nullable ContainerMenu getOpenMenu() {
        return this.openMenu;
    }

    public void openMenu(ContainerMenu menu) {
        if (this.openMenu != null) {
            if (this.openMenu == menu)
                return;
            this.closeMenu();
        }

        this.openMenu = menu;
        this.openMenu.addWatcher(this);
    }

    public void closeMenu() {
        if (this.openMenu == null) {
            CommonConstants.LOGGER.warn("Tried to close menu that was not open", new RuntimeException());
            return;
        }

        if (this instanceof ServerPlayer serverPlayer) {
            MenuEvents.MENU_CLOSE.factory().onMenuClose(this.openMenu, serverPlayer);
        }

        if (!(this.openMenu instanceof Inventory)) {
            this.openMenu.removeWatcher(this);
        }
        this.openMenu = null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This also clamps the player's position to {@code -30000000..30000000}
     * And other handling of invalid positions
     */
    @Override
    protected void onMoved() {
        this.x = Mth.clamp(this.x, -30000000, 30000000);
        this.z = Mth.clamp(this.z, -30000000, 30000000);

        super.onMoved();
    }

    public ItemStack getCursor() {
        return this.cursor;
    }

    /**
     * Set the item cursor for menu containers.
     * <p style="color:red;"><b>Not recommended to be called on client side, only do it when you know what you're doing.</b></p>
     *
     * @param cursor the item stack to set.
     */
    public void setCursor(ItemStack cursor) {
        Preconditions.checkNotNull(cursor, "cursor");
        this.cursor = cursor;
    }

    public void openInventory() {
        this.openMenu(this.inventory);
    }

    public @Nullable Entity rayCast(Iterable<Entity> entities) {
        Ray ray = new Ray(this.getPosition(), this.getLookVector());
        boolean seen = false;
        Entity best = null;
        Comparator<Entity> comparator = Comparator.comparing(entity -> entity.getPosition().dst(ray.origin));
        for (Entity entity : entities) {
            if (Intersector.intersectRayBounds(ray, entity.getBoundingBox(), null) && (!seen || comparator.compare(entity, best) < 0)) {
                seen = true;
                best = entity;
            }
        }
        return seen ? best : null;
    }

    public @Nullable Entity nearestEntity() {
        return Arrays.stream(this.world.getEntities()
                .toArray(Entity.class))
                .min(Comparator.comparing(entity -> entity.getPosition().dst(this.getPosition())))
                .orElse(null);
    }

    public @Nullable <T extends Entity> T nearestEntity(Class<T> clazz) {
        return Arrays.stream(this.world.getEntities()
                .toArray(Entity.class))
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .min(Comparator.comparing(entity -> entity.getPosition().dst(this.getPosition())))
                .orElse(null);
    }

    public void setGameMode(GameMode gamemode) {
        this.gamemode = gamemode;
        switch (gamemode) {
            case SURVIVAL:
                this.abilities.allowFlight = false;
                this.abilities.instaMine = false;
                this.abilities.invincible = false;
                this.abilities.blockBreak = true;
                this.abilities.flying = false;
                this.noClip = false;
                break;
            case BUILDER:
            case BUILDER_PLUS:
                this.abilities.allowFlight = true;
                this.abilities.instaMine = true;
                this.abilities.invincible = true;
                this.abilities.blockBreak = true;
                this.noClip = false;
                break;
            case ADVENTUROUS:
                this.abilities.allowFlight = false;
                this.abilities.instaMine = false;
                this.abilities.invincible = false;
                this.abilities.blockBreak = false;
                this.abilities.flying = false;
                this.noClip = false;
                break;
            case SPECTATOR:
                this.abilities.allowFlight = true;
                this.abilities.instaMine = false;
                this.abilities.invincible = true;
                this.abilities.blockBreak = false;
                this.abilities.flying = true;
                this.noClip = true;
                break;
        }

        this.sendAbilities();
    }

    public GameMode getGamemode() {
        return this.gamemode;
    }

    public boolean isBuilder() {
        return this.gamemode == GameMode.BUILDER || this.gamemode == GameMode.BUILDER_PLUS;
    }

    public boolean isSurvival() {
        return this.gamemode == GameMode.SURVIVAL || this.gamemode == GameMode.ADVENTUROUS;
    }

    public void drop(ItemStack itemStack) {
        this.world.drop(itemStack, this.getPosition());
        ItemEvents.DROPPED.factory().onDropped(itemStack);
    }

    public void dropItem() {
        if (this.world.isClientSide()) return;
        ItemStack itemStack = this.getSelectedItem();
        if (itemStack.shrink(1) != 0) return;

        ItemStack copy = itemStack.copy();
        copy.setCount(1);
        this.drop(copy);
    }

    public void closeMenu(CrateMenu menu) {
        if (this.openMenu == menu) {
            this.closeMenu();
        }
    }

    public FoodStatus getFoodStatus() {
        return this.foodStatus;
    }

    public boolean isSwimming() {
        return this.isInWater() && (this.x != this.ox || this.z != this.oz || this.y > this.oy);
    }

    public float getReach() {
        return reach;
    }

    public void setReach(float reach) {
        this.reach = reach;
    }
}
