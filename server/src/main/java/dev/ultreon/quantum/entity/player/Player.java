package dev.ultreon.quantum.entity.player;

import com.google.common.base.Preconditions;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.entity.Attribute;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.events.ItemEvents;
import dev.ultreon.quantum.events.MenuEvents;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.*;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.sound.event.SoundEvents;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * The Player class represents a player entity within the game. It manages various player-specific details such as inventory,
 * movement, abilities, and status effects.
 */
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

    /**
     * Constructs a Player entity in the specified world with a given entity type and name.
     * Initializes the player's inventory and sets up inventory related actions.
     *
     * @param entityType the type of entity
     * @param world the world the player is in
     * @param name the name of the player
     */
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
        this.sendAbilities();
    }

    /**
     * Selects a block slot in the player's inventory based on the given index.
     * The selection wraps around the available slots (0-8).
     *
     * @param i the index to select. This value is normalized to a valid
     *          slot (i.e., within 0-8) using the modulus operation.
     */
    public void selectBlock(int i) {
        int toSelect = i % 9;
        if (toSelect < 0) toSelect += 9;
        this.selected = toSelect;
    }

    /**
     * Retrieves the currently selected item from the player's hotbar.
     * If the selected slot is out of bounds, returns an empty ItemStack.
     *
     * @return the ItemStack in the selected hotbar slot, or an empty ItemStack if out of bounds.
     */
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

    /**
     * Rotates the player's head by the specified amounts in the x and y directions.
     * The y-axis rotation (pitch) is clamped between -90 and 90 degrees ensuring the player cannot look too far up or down.
     * The x-axis rotation (yaw) is adjusted and clamped to stay within 50 degrees of the current head rotation.
     *
     * @param x the amount to rotate the head on the x-axis (yaw)
     * @param y the amount to rotate the head on the y-axis (pitch)
     */
    public void rotateHead(float x, float y) {
        this.xHeadRot += x;
        this.yRot = Mth.clamp(this.yRot + y, -90, 90);
        this.xRot = Mth.clamp(this.xRot, this.xHeadRot - 50, this.xHeadRot + 50);
    }

    /**
     * Returns the eye height of the player, considering if the player is crouching or not.
     *
     * @return the eye height of the player. Returns 1.15 when crouching, otherwise returns 1.63.
     */
    public float getEyeHeight() {
        return this.crouching ? 1.15F : 1.63F;
    }

    /**
     * Checks whether the player is currently running.
     * A player is considered running if they are marked as running, have moved from their previous position,
     * and are currently walking.
     *
     * @return true if the player is running; false otherwise.
     */
    public boolean isRunning() {
        return this.running && (this.ox != this.x || this.oz != this.z || this.oy != this.y) & isWalking();
    }

    /**
     * Sets the running state of the player.
     *
     * @param running boolean value indicating whether the player is running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Retrieves the player's walking speed. If the player is currently running,
     * the walking speed is multiplied by the run modifier.
     *
     * @return the walking speed, adjusted for running if applicable.
     */
    public float getWalkingSpeed() {
        return this.isRunning() ? this.walkingSpeed * this.runModifier : this.walkingSpeed;
    }

    /**
     * Sets the walking speed for the player.
     *
     * @param walkingSpeed the new walking speed value to set
     */
    public void setWalkingSpeed(float walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    /**
     * Retrieves the flying speed of the player.
     * The speed is modified by the running modifier if the player is running.
     *
     * @return the calculated flying speed of the player.
     */
    public float getFlyingSpeed() {
        return isRunning() ? this.flyingSpeed * this.runModifier : this.flyingSpeed;
    }

    /**
     * Sets the flying speed of the player.
     *
     * @param flyingSpeed the new flying speed to set
     */
    public void setFlyingSpeed(float flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    /**
     * Checks if the player is currently in a flying state.
     *
     * @return true if the player is flying, false otherwise.
     */
    public boolean isFlying() {
        return this.abilities.flying;
    }

    /**
     * Sets the player's flying state and updates related abilities.
     *
     * @param flying the new flying state to set. If true, enables flying and disables gravity;
     *               if false, disables flying and enables gravity.
     */
    public void setFlying(boolean flying) {
        this.noGravity = this.abilities.flying = flying;
        this.sendAbilities();
    }

    /**
     * Checks if the player is currently in the crouching state.
     *
     * @return true if the player is crouching, false otherwise.
     */
    public boolean isCrouching() {
        return this.crouching;
    }

    /**
     * Sets the crouching state of the player.
     *
     * @param crouching true to set the player as crouching, false to set the player as not crouching.
     */
    public void setCrouching(boolean crouching) {
        this.crouching = crouching;
    }

    /**
     * Checks if the player is currently in spectator mode.
     *
     * @return true if the player's game mode is SPECTATOR, false otherwise
     */
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

    @Nullable
    @Override
    public SoundEvent getHurtSound() {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected final void removeDead() {
        // Don't remove dead players, removing them causes the player to get completely unresponsive
        //    and will break client <-> server connection. DO NOT CALL SUPER HERE!
    }

    /**
     * Performs a ray casting operation from the player's current position and orientation
     * to determine if a collision with another object occurs within the player's reach.
     *
     * @return a Hit object that contains details about the collision, including distance,
     *         collision vector, and whether a collision occurred.
     */
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
        this.inventory.load(data.getList("Inventory"));
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
        data.put("Inventory", this.inventory.save());

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

    /**
     * Determines if the player is permitted to fly.
     *
     * @return true if the player is allowed to fly; false otherwise.
     */
    public boolean isAllowFlight() {
        return this.abilities.allowFlight;
    }

    /**
     * Sets whether the player is allowed to fly.
     *
     * @param allowFlight boolean value indicating whether flight is allowed
     */
    public void setAllowFlight(boolean allowFlight) {
        this.abilities.allowFlight = allowFlight;
        this.sendAbilities();
    }

    /**
     * Sends the current abilities of the player to the client/server.
     */
    protected abstract void sendAbilities();

    /**
     * Handles the player's abilities based on the information provided by an AbilitiesPacket.
     *
     * @param packet the AbilitiesPacket containing information about the player's current abilities,
     *               such as whether the player is flying.
     */
    protected void onAbilities(AbilitiesPacket packet) {
        this.noGravity = packet.flying();
    }

    /**
     * Retrieves the currently open menu of the player.
     *
     * @return the currently open {@link Menu} if a menu is open, otherwise null.
     */
    public @Nullable ContainerMenu getOpenMenu() {
        return this.openMenu;
    }

    /**
     * Opens the specified menu for the player.
     * If the player already has an open menu, it will close the current menu
     * before opening the new one.
     *
     * @param menu the {@link Menu} to open.
     */
    public void openMenu(ContainerMenu menu) {
        if (this.openMenu != null) {
            if (this.openMenu == menu)
                return;
            this.closeMenu();
        }

        this.openMenu = menu;
        this.openMenu.addWatcher(this);
    }

    /**
     * Closes the currently open menu for this player.
     */
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

    /**
     * Opens the inventory menu for the player or user.
     */
    public void openInventory() {
        this.openMenu(this.inventory);
    }

    /**
     * Casts a ray in the direction the current entity is looking and determines the closest intersecting entity.
     *
     * @param entities An iterable collection of entities to check for intersections.
     * @return The closest entity intersecting with the ray, or null if no intersection is found.
     */
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

    /**
     * Finds and returns the nearest entity to the current entity based on their positions.
     *
     * @return the nearest entity if any are present, otherwise null
     */
    public @Nullable Entity nearestEntity() {
        return Arrays.stream(this.world.getEntities()
                .toArray(Entity.class))
                .min(Comparator.comparing(entity -> entity.getPosition().dst(this.getPosition())))
                .orElse(null);
    }

    /**
     * Finds the nearest entity of the specified class type within the world.
     *
     * @param <T> The type of the entity that extends the Entity class.
     * @param clazz The class type of the entity to find.
     * @return The nearest entity of the specified class type, or null if no such entity is found.
     */
    public @Nullable <T extends Entity> T nearestEntity(Class<T> clazz) {
        return Arrays.stream(this.world.getEntities()
                .toArray(Entity.class))
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .min(Comparator.comparing(entity -> entity.getPosition().dst(this.getPosition())))
                .orElse(null);
    }

    /**
     * Sets the game mode for the player and adjusts their abilities accordingly.
     *
     * @param gamemode the new game mode to set for the player
     */
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

    /**
     * Retrieves the current game mode.
     *
     * @return the current GameMode
     */
    public GameMode getGamemode() {
        return this.gamemode;
    }

    /**
     * Determines if the current game mode is either BUILDER or BUILDER_PLUS.
     *
     * @return true if the game mode is BUILDER or BUILDER_PLUS, false otherwise
     */
    public boolean isBuilder() {
        return this.gamemode == GameMode.BUILDER || this.gamemode == GameMode.BUILDER_PLUS;
    }

    /**
     * Checks if the current game mode is either SURVIVAL or ADVENTUROUS.
     *
     * @return true if the game mode is SURVIVAL or ADVENTUROUS, false otherwise.
     */
    public boolean isSurvival() {
        return this.gamemode == GameMode.SURVIVAL || this.gamemode == GameMode.ADVENTUROUS;
    }

    /**
     * Drops the specified item stack at the current entity's position in the world.
     *
     * @param itemStack the item stack to be dropped in the world
     */
    public void drop(ItemStack itemStack) {
        this.world.drop(itemStack, this.getPosition());
        ItemEvents.DROPPED.factory().onDropped(itemStack);
    }

    /**
     * Handles the logic for dropping a single item from the player's selected item stack.
     * The method first checks if the world is on the client side; if so, it exits immediately.
     * If the selected item stack size is reduced and becomes zero, the process ends.
     * Otherwise, a copy of the item stack is created with a count of one,
     * and this copy is dropped into the world.
     */
    public void dropItem() {
        if (this.world.isClientSide()) return;
        ItemStack itemStack = this.getSelectedItem();
        if (itemStack.shrink(1) != 0) return;

        ItemStack copy = itemStack.copy();
        copy.setCount(1);
        this.drop(copy);
    }

    /**
     * Closes the specified menu if it is the currently open menu.
     *
     * @param menu the menu that might need to be closed
     */
    public void closeMenu(CrateMenu menu) {
        if (this.openMenu == menu) {
            this.closeMenu();
        }
    }

    /**
     * Retrieves the current food status of the player.
     *
     * @return the player's current {@code FoodStatus}.
     */
    public FoodStatus getFoodStatus() {
        return this.foodStatus;
    }

    /**
     * Determines if the entity is currently swimming.
     *
     * @return true if the entity is in water and there is a significant change in position
     *         indicating movement, otherwise false.
     */
    public boolean isSwimming() {
        return this.isInWater() && (this.x != this.ox || this.z != this.oz || this.y > this.oy);
    }

    /**
     * Retrieves the player's reach distance.
     *
     * @return the reach distance of the player.
     */
    public float getReach() {
        return reach;
    }

    /**
     * Sets the reach distance for this player. The reach distance determines how far
     * the player can interact with blocks and entities in the world.
     *
     * @param reach the new reach distance to set
     */
    public void setReach(float reach) {
        this.reach = reach;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return getUuid() == player.getUuid();
    }

    @Override
    public int hashCode() {
        UUID uuid = getUuid();
        if (uuid == null) return 0;
        return uuid.hashCode();
    }
}
