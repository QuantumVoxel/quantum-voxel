package dev.ultreon.quantum.client.player;

import java.util.*;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.screens.ChunkLoadScreen;
import dev.ultreon.quantum.client.gui.screens.DeathScreen;
import dev.ultreon.quantum.client.gui.screens.container.ContainerScreen;
import dev.ultreon.quantum.client.input.GameInput;
import dev.ultreon.quantum.client.registry.MenuRegistry;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.MenuType;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SAbilitiesPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SCloseMenuPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SDropItemPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SHotbarIndexPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SOpenInventoryPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SOpenMenuPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SPlayerMoveAndRotatePacket;
import dev.ultreon.quantum.network.packets.c2s.C2SPlayerMovePacket;
import dev.ultreon.quantum.network.packets.c2s.C2SRequestChunkLoadPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SUnloadChunkPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CPlayerHurtPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CTemperatureSyncPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.sound.SoundType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.Location;
import dev.ultreon.quantum.world.SoundEvent;
import static dev.ultreon.quantum.world.World.CS;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;

/**
 * Represents a local player entity in the game.
 * <p>
 * This class extends ClientPlayer and provides additional functionality for
 * local players.
 * </p>
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class LocalPlayer extends ClientPlayer {

    private final QuantumClient client = QuantumClient.get();
    public boolean movedLastFrame;
    private ClientWorldAccess clientWorld;
    private int oldSelected;
    private final ClientPermissionMap permissions = new ClientPermissionMap();
    private double lastWalkSound;
    private final Vec3d tmp = new Vec3d();
    private final Vec2i tmp2I = new Vec2i();
    private final Set<ChunkVec> chunksToLoad = Collections.synchronizedSet(new HashSet<>());
    private long lastRefresh;

    /**
     * The set of pending chunks to be loaded.
     */
    public final Set<ChunkVec> pendingChunks = Collections.synchronizedSet(new HashSet<>());

    /**
     * The queue of chunks to be sent to the server.
     */
    private final Queue<ChunkVec> sendQueue = new ArrayDeque<>();
    private boolean isLoading;

    /**
     * Constructs a new LocalPlayer.
     *
     * @param entityType The entity type.
     * @param world The client world access.
     * @param uuid The UUID of the player.
     */
    public LocalPlayer(EntityType<? extends Player> entityType, ClientWorldAccess world, UUID uuid) {
        super(entityType, world);
        this.clientWorld = world;
        this.setUuid(uuid);

        this.health = this.getMaxHealth();
    }

    /**
     * Overrides the tick method to handle player actions and movement in the
     * game.
     * <p>
     * This method is called every tick to handle player actions and movement.
     * </p>
     */
    @Override
    public void tick() {
        // Do not execute if not rendering the world
        if (!this.client.renderWorld) {
            return;
        }

        // Determine if the player is jumping based on input
        this.jumping = !this.isDead() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) && Gdx.input.isCursorCatched());

        var connection = this.client.connection;
        if (xRot != oXRot || yRot != oYRot || xHeadRot != oXHeadRot) {
            if (connection != null) {
                connection.send(new C2SPlayerMoveAndRotatePacket(this.x, this.y, this.z, this.xHeadRot, this.xRot, this.yRot));
            }
        }
        // Call the superclass tick method
        super.tick();

        // Send a packet when the selected item changes
        if (this.selected != this.oldSelected) {
            if (connection != null) {
                connection.send(new C2SHotbarIndexPacket(this.selected));
            }
            this.oldSelected = this.selected;
        }

        // Handle player movement if there is a significant change in position
        if (this.clientWorld.getChunk(this.getChunkVec()) == null && !this.isSpectator()) {
            this.x = this.ox;
            this.y = this.oy;
            this.z = this.oz;

            this.refreshChunks();
        } else {
            if (Math.abs(this.x - this.ox) >= 0.01 || Math.abs(this.y - this.oy) >= 0.01 || Math.abs(this.z - this.oz) >= 0.01) {
                double dst = tmp.set(this.x, this.y, this.z).dst(this.ox, this.oy, this.oz);
                if (isWalking() && onGround && this.lastWalkSound + 0.2 / dst < this.client.getGameTime() && !this.isSpectator()) {
                    SoundEvent walkSound = getWalkSound();
                    if (walkSound != null) {
                        this.client.playSound(walkSound, 1.0F);
                    }
                    this.lastWalkSound = this.client.getGameTime();
                }
                this.handleMove();
            } else {
                // Update previous position if no significant change
                this.ox = this.x;
                this.oy = this.y;
                this.oz = this.z;
            }
        }

        movedLastFrame = x != ox || y != oy || z != oz;

        ChunkVec toLoad = this.sendQueue.poll();
        if (toLoad != null && connection != null) {
            connection.send(new C2SRequestChunkLoadPacket(toLoad));
        }
    }

    /**
     * Gets the walk sound for the player.
     * <p>
     * This method returns the sound event for the player's walk sound.
     * </p>
     *
     * @return The walk sound for the player.
     */
    @Nullable
    protected SoundEvent getWalkSound() {
        BlockState state = this.getOnBlock();
        SoundType soundType = state.getBlock().getSoundType(state, this.clientWorld, this.getBlockVec());
        return soundType.getStepSound(getRng());
    }

    /**
     * Gets the block the player is on.
     * <p>
     * This method returns the block the player is on.
     * </p>
     *
     * @return The block the player is on.
     */
    protected BlockState getOnBlock() {
        if (this.clientWorld == null) {
            return Blocks.AIR.getDefaultState();
        }
        return this.clientWorld.get(this.getBlockVec().below());
    }

    /**
     * Handles the player movement by sending the new coordinates to the server.
     */
    protected void handleMove() {
        // Send the player's new coordinates to the server
        if (this.client.connection != null) {
            if (xRot != oXRot || yRot != oYRot || xHeadRot != oXHeadRot) {
                this.client.connection.send(new C2SPlayerMoveAndRotatePacket(this.x, this.y, this.z, this.xHeadRot, this.xRot, this.yRot));
            } else {
                this.client.connection.send(new C2SPlayerMovePacket(this.x, this.y, this.z));
            }
        }

        // Update the old coordinates to the current ones
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;

        this.refreshChunks();
    }

    /**
     * Refreshes the chunks around the player based on the current chunk position and render distance.
     * This method is designed to be called periodically to ensure the player has the correct chunks loaded and
     * unload any chunks that are no longer within the render distance.
     */
    public void refreshChunks() {
        if (!this.client.renderWorld) {
            return;
        }
        if (lastRefresh + 1000 > System.currentTimeMillis()) {
            return;
        }
        lastRefresh = System.currentTimeMillis();

        IConnection<ClientPacketHandler, ServerPacketHandler> connection = this.client.connection;
        ChunkVec chunkVec = this.getChunkVec();

        if (connection == null) {
            return;
        }
        int renderDistance = Math.max(2, ClientConfiguration.renderDistance.getValue() / CS);

        for (ClientChunkAccess chunk : this.clientWorld.getLoadedChunks()) {
            if (chunk.getVec().dst(chunkVec) > renderDistance) {
                this.unloadChunk(chunk);
                this.client.connection.send(new C2SUnloadChunkPacket(chunk.getVec()));
            }
        }

        Set<ChunkVec> loadingChunks = this.chunksToLoad;
        loadingChunks.clear();
        int renderDistanceSquared = renderDistance * renderDistance;
        for (int deltaX = -renderDistance; deltaX <= renderDistance; deltaX++) {
            for (int deltaY = -renderDistance; deltaY <= renderDistance; deltaY++) {
                for (int deltaZ = -renderDistance; deltaZ <= renderDistance; deltaZ++) {
                    int distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                    if (distanceSquared > renderDistanceSquared) {
                        continue;
                    }

                    ChunkVec relativePos = new ChunkVec(chunkVec.getIntX() + deltaX, chunkVec.getIntY() + deltaY, chunkVec.getIntZ() + deltaZ, ChunkVecSpace.WORLD);
                    if (this.clientWorld.getChunk(relativePos) != null || this.pendingChunks.contains(relativePos)) {
                        continue;
                    }

                    loadingChunks.add(relativePos);
                }
            }
        }
        if (!this.chunksToLoad.isEmpty()) {
            Stream<ChunkVec> sorted = loadingChunks.stream().sorted(Comparator.comparing(chunkVec1 -> this.tmp2I.set(chunkVec1.getIntX(), chunkVec1.getIntZ()).dst(chunkVec.getIntX(), chunkVec.getIntZ())));
            sorted.forEachOrdered(e -> {
                this.pendingChunks.add(e);
                this.sendQueue.add(e);
            });
        }

        this.isLoading = false;
    }

    /**
     * Sets the position of the player.
     * <p>
     * This method sets the position of the player and refreshes the chunks.
     * </p>
     *
     * @param position The position to set.
     */
    @Override
    public void setPosition(@NotNull Vec3d position) {
        super.setPosition(position);

        this.refreshChunks();
    }

    /**
     * Sets the position of the player.
     * <p>
     * This method sets the position of the player and refreshes the chunks.
     * </p>
     *
     * @param x The x position to set.
     * @param y The y position to set.
     * @param z The z position to set.
     */
    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);

        this.refreshChunks();
    }

    /**
     * Unloads a chunk.
     * <p>
     * This method unloads a chunk from the client world.
     * </p>
     *
     * @param chunk The chunk to unload.
     */
    public void unloadChunk(ClientChunkAccess chunk) {
        this.clientWorld.unloadChunk(chunk.getVec());
    }

    /**
     * This will do nothing on the client side of the player.
     */
    @Override
    protected void hitGround() {
        // Do nothing on the client side of the player.
    }

    /**
     * Checks if the player is walking.
     * <p>
     * This method checks if the player is walking.
     * </p>
     *
     * @return True if the player is walking, false otherwise.
     */
    @Override
    public boolean isWalking() {
        return this.client.playerInput.isWalking() && (ox != x || oy != y || oz != z);
    }

    /**
     * This method will do nothing on the client side of the player.
     */
    @Override
    protected void hurtFromVoid() {
        // The server should handle player void damage.
        this.onVoidDamage();
    }

    /**
     * This method will do nothing on the client side of the player.
     */
    @Override
    public void jump() {
        if (isAffectedByFluid()) {
            this.swimUp();
            return;
        }

        this.velocityY = this.jumpVel;

        if (this.isRunning()) {
            this.velocityX *= 1.2;
            this.velocityZ *= 1.2;
        }
    }

    /**
     * This method will do nothing on the client side of the player.
     */
    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        if (source == DamageSource.FALLING) {
            GameInput.startVibration(50, 1.0F);
        }

        return super.onHurt(damage, source);
    }

    /**
     * This method will show the death screen.
     */
    @Override
    public void onDeath(@NotNull DamageSource source) {
        super.onDeath(source);

        this.client.showScreen(new DeathScreen(source));
    }

    /**
     * This method will start the vibration.
     */
    @Override
    protected void onVoidDamage() {
        GameInput.startVibration(200, 1.0F);

        super.onVoidDamage();
    }

    /**
     * If the sound event is not null, it also plays the sound using the client.
     *
     * @param sound The sound event to be played. Can be null.
     * @param volume The volume at which the sound should be played.
     */
    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        // Call the superclass method to play the sound event and volume
        super.playSound(sound, volume);

        // If the sound event is not null, play the sound using the client
        if (sound != null) {
            this.client.playSound(sound, volume);
        }
    }

    /**
     * This method will send the player's abilities to the server.
     */
    @Override
    protected void sendAbilities() {
        if (this.client.connection == null) {
            return;
        }
        this.client.connection.send(new C2SAbilitiesPacket(this.abilities));
    }

    /**
     * Updates the player's abilities based on the information received in the
     * AbilitiesPacket.
     *
     * @param packet The AbilitiesPacket containing the player's abilities
     * information.
     */
    @Override
    public void onAbilities(@NotNull AbilitiesPacket packet) {
        // Update the player's abilities
        this.abilities.flying = packet.flying();
        this.abilities.allowFlight = packet.allowFlight();
        this.abilities.instaMine = packet.isInstaMine();
        this.abilities.invincible = packet.isInvincible();

        // Call superclass method
        super.onAbilities(packet);
    }

    /**
     * This method will open the menu specified if it is not already open.
     *
     * @param menu The menu to open.
     */
    @Override
    public void openMenu(@NotNull ContainerMenu menu) {
        if (this.openMenu != null) {
            return;
        }

        super.openMenu(menu);

        if (menu != inventory) {
            this.client.connection.send(new C2SOpenMenuPacket(menu.getType().getId(), menu.getPos()));
        }
    }

    /**
     * This method will close the menu.
     */
    @Override
    public void closeMenu() {
        super.closeMenu();

        if (this.client.connection != null) {
            this.client.connection.send(new C2SCloseMenuPacket());
        }
    }

    /**
     * This method will open the inventory.
     */
    @Override
    public void openInventory() {
        super.openInventory();
        if (this.client.connection != null) {
            this.client.connection.send(new C2SOpenInventoryPacket());
        }
    }

    /**
     * This method will return the client world.
     */
    @Override
    public @NotNull
    WorldAccess getWorld() {
        return this.clientWorld;
    }

    /**
     * This method will update the player's health.
     */
    public void onHealthUpdate(float newHealth) {
        this.oldHealth = this.health;
        this.health = newHealth;
    }

    /**
     * This method will resurrect the player.
     */
    public void resurrect() {
        this.setHealth(this.getMaxHealth());
        this.isDead = false;
    }

    /**
     * This method will open the menu specified if it is not already open.
     *
     * @param menuType The type of menu to open.
     * @param items The items to open the menu with.
     */
    public void onOpenMenu(MenuType<?> menuType, List<ItemStack> items) {
        ContainerMenu openedBefore = openMenu;
        if (openedBefore != null && menuType != openedBefore.getType()) {
            CommonConstants.LOGGER.warn("Opened menu {} instead of {}", menuType, openMenu);
        } else if (openedBefore == null) {
            CommonConstants.LOGGER.warn("Opened server menu {} before opening any on client side", menuType);
            if (this.clientWorld instanceof ClientWorld) {
                ClientWorld ourClientWorld = (ClientWorld) this.clientWorld;
                openedBefore = menuType.create(ourClientWorld, this, this.getBlockVec());
            }
        }
        ContainerScreen screen = MenuRegistry.getScreen(openedBefore);
        screen.setup(items);
        this.client.showScreen(screen);
    }

    /**
     * This method will return the location of the player.
     *
     * @return The location of the player.
     */
    @Override
    public @NotNull
    Location getLocation() {
        return new Location(this.clientWorld, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    /**
     * This method will return true if the player has explicit permission for
     * the given permission.
     *
     * @param permission The permission to check.
     * @return True if the player has explicit permission for the given
     * permission, false otherwise.
     */
    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return this.permissions.has(permission);
    }

    /**
     * This method will drop the item.
     */
    @Override
    public void dropItem() {
        this.client.connection.send(new C2SDropItemPacket());
    }

    /**
     * This method will return the permissions of the player.
     *
     * @return The permissions of the player.
     */
    public ClientPermissionMap getPermissions() {
        return this.permissions;
    }

    /**
     * This method will hurt the player.
     *
     * @param packet The packet to hurt the player with.
     */
    public void onHurt(S2CPlayerHurtPacket packet) {
        this.hurt(packet.damage(), packet.source());
    }

    /**
     * Returns the position with interpolation based on the partial tick.
     *
     * @param partialTick the partial tick for interpolation
     * @return the interpolated position as a Vec3d object
     */
    public Vec3d getPosition(float partialTick) {
        return new Vec3d(
                Mth.lerp(this.ox, this.x, partialTick),
                Mth.lerp(this.oy, this.y, partialTick),
                Mth.lerp(this.oz, this.z, partialTick)
        );
    }

    /**
     * This method will return the position of the player.
     *
     * @return The position of the player.
     */
    @Override
    @Deprecated
    public @NotNull
    Vec3d getPosition() {
        return super.getPosition();
    }

    /**
     * This method will return the block state of the player.
     *
     * @return The block state of the player.
     */
    @Override
    public @NotNull
    BlockState getBuriedBlock() {
        Vec3d add = this.getPosition(this.client.partialTick).add(0, getEyeHeight(), 0);
        return this.clientWorld.get((int) Math.floor(add.x), (int) Math.floor(add.y), (int) Math.floor(add.z));
    }

    /**
     * This method will teleport the player to the new world.
     *
     * @param world The new world to teleport the player to.
     */
    public void onTeleportedDimension(ClientWorldAccess world) {
        super.onTeleportedDimension(world);

        pendingChunks.clear();
        isLoading = true;
        client.showScreen(new ChunkLoadScreen(() -> !isLoading && pendingChunks.isEmpty()));
        this.clientWorld = world;
    }

    /**
     * This method will do nothing on the client side of the player.
     *
     * @param world The new world to teleport the player to.
     */
    @Override
    @Deprecated
    public void onTeleportedDimension(@NotNull WorldAccess world) {
        super.onTeleportedDimension(world);
    }

    /**
     * This method will update the player's temperature.
     *
     * @param packet The packet to update the player's temperature with.
     */
    public void onTemperatureSync(S2CTemperatureSyncPacket packet) {
        double temperature0 = packet.temperature();
        this.temperature = temperature0;
        this.temperatureGoal = temperature0;
    }

    /**
     * This method will update the menu.
     *
     * @param menuId The menu id to update.
     * @param stack The stack to update the menu with.
     */
    public void onMenuChanged(@NotNull NamespaceID menuId, ItemStack @NotNull [] stack) {
        ContainerMenu currentMenu = this.openMenu;
        if (currentMenu != null && currentMenu.getType().getId().equals(menuId)) {
            currentMenu.setAll(stack);
        }
    }
}
