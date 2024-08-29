package dev.ultreon.quantum.client.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
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
import dev.ultreon.quantum.network.packets.c2s.*;
import dev.ultreon.quantum.network.packets.s2c.S2CPlayerHurtPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.sound.SoundType;
import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.Location;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

public class LocalPlayer extends ClientPlayer {
    private final QuantumClient client = QuantumClient.get();
    private final ClientWorldAccess world;
    private int oldSelected;
    private final ClientPermissionMap permissions = new ClientPermissionMap();
    private double lastWalkSound;
    private final Vec3d tmp = new Vec3d();
    private final Vec2i tmp2I = new Vec2i();
    private final Set<ChunkVec> chunksToLoad = new CopyOnWriteArraySet<>();
    private long lastRefresh;
    public final Set<ChunkVec> pendingChunks = new HashSet<>();

    public LocalPlayer(EntityType<? extends Player> entityType, ClientWorldAccess world, UUID uuid) {
        super(entityType, world);
        this.world = world;
        this.setUuid(uuid);
    }

    /**
     * Overrides the tick method to handle player actions and movement in the game.
     */
    @Override
    public void tick() {
        // Do not execute if not rendering the world
        if (!this.client.renderWorld) return;

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
            if (connection != null)
                connection.send(new C2SHotbarIndexPacket(this.selected));
            this.oldSelected = this.selected;
        }

        // Handle player movement if there is a significant change in position
        if (this.world.getChunk(this.getChunkVec()) == null && !this.isSpectator()) {
            this.x = this.ox;
            this.y = this.oy;
            this.z = this.oz;

            this.refreshChunks();
        } else {
            if (Math.abs(this.x - this.ox) >= 0.01 || Math.abs(this.y - this.oy) >= 0.01 || Math.abs(this.z - this.oz) >= 0.01) {
                double dst = tmp.set(this.x, this.y, this.z).dst(this.ox, this.oy, this.oz);
                if (isWalking() && onGround && this.lastWalkSound + 0.2 / dst < this.client.getGameTime() && !this.isSpectator()) {
                    SoundEvent walkSound = getWalkSound();
                    if (walkSound != null)
                        this.client.playSound(walkSound, 1.0F);
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
    }

    private @Nullable SoundEvent getWalkSound() {
        BlockState state = this.getOnBlock();
        SoundType soundType = state.getBlock().getSoundType(state, this.world, this.getBlockVec());
        return soundType.getStepSound(getRng());
    }

    private BlockState getOnBlock() {
        if (this.world == null) return BlockState.AIR;
        return this.world.get(this.getBlockVec().below());
    }


    /**
     * Handles the player movement by sending the new coordinates to the server.
     */
    private void handleMove() {
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

    private void refreshChunks() {
        if (!this.client.renderWorld) return;
        if (lastRefresh + 1000 > System.currentTimeMillis()) return;
        lastRefresh = System.currentTimeMillis();

        IConnection<ClientPacketHandler, ServerPacketHandler> connection = this.client.connection;
        ChunkVec chunkVec = this.getChunkVec();

        if (connection == null) return;
        int renderDistance = ClientConfig.renderDistance;

        for (ClientChunkAccess chunk : this.world.getLoadedChunks()) {
            if (chunk.getVec().dst(chunkVec) > renderDistance) {
                this.unloadChunk(chunk);
                this.client.connection.send(new C2SUnloadChunkPacket(chunk.getVec()));
            }
        }

        Set<ChunkVec> chunksToLoad = this.chunksToLoad;
        chunksToLoad.clear();
        ChunkVec chunkPos = this.getChunkVec();
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int y = -renderDistance; y <= renderDistance; y++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    ChunkVec relativePos = new ChunkVec(chunkVec.getIntX() + x, chunkVec.getIntY() + y, chunkVec.getIntZ() + z, ChunkVecSpace.WORLD);
                    if (this.pendingChunks.contains(relativePos) || this.world.getChunk(relativePos) != null) continue;
                    if (chunkPos.dst(relativePos) <= renderDistance && !this.world.isLoaded(relativePos)) {
                        chunksToLoad.add(relativePos);
                    }
                }
            }
        }

        if (!this.chunksToLoad.isEmpty()) {
            Stream<ChunkVec> sorted = chunksToLoad.stream().sorted(Comparator.comparing(chunkVec1 -> this.tmp2I.set(chunkVec1.getIntX(), chunkVec1.getIntZ()).dst(chunkVec.getIntX(), chunkVec.getIntZ())));
            sorted.forEachOrdered(toLoad -> {
                connection.send(new C2SRequestChunkLoadPacket(toLoad));
                this.pendingChunks.add(toLoad);
            });
        }
    }

    @Override
    public void setPosition(@NotNull Vec3d position) {
        super.setPosition(position);

        this.refreshChunks();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);

        this.refreshChunks();
    }

    private void unloadChunk(ClientChunkAccess chunk) {
        this.world.unloadChunk(chunk.getVec());
    }

    /**
     * {@inheritDoc}
     * This will do nothing on the client side of the player.
     */
    @Override
    protected void hitGround() {
        // Do nothing on the client side of the player.
    }

    @Override
    public boolean isWalking() {
        return this.client.playerInput.isWalking() && (ox != x || oy != y || oz != z);
    }

    @Override
    protected void hurtFromVoid() {
        // The server should handle player void damage.
        this.onVoidDamage();
    }

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

    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        if (source == DamageSource.FALLING) {
            GameInput.startVibration(50, 1.0F);
        }

        return super.onHurt(damage, source);
    }

    @Override
    public void onDeath(@NotNull DamageSource source) {
        super.onDeath(source);

        this.client.showScreen(new DeathScreen(source));
    }

    @Override
    protected void onVoidDamage() {
        GameInput.startVibration(200, 1.0F);

        super.onVoidDamage();
    }

    /**
     * {@inheritDoc}
     * If the sound event is not null, it also plays the sound using the client.
     *
     * @param sound  The sound event to be played. Can be null.
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

    @Override
    protected void sendAbilities() {
        if (this.client.connection == null) return;
        this.client.connection.send(new C2SAbilitiesPacket(this.abilities));
    }

    /**
     * Updates the player's abilities based on the information received in the AbilitiesPacket.
     *
     * @param packet The AbilitiesPacket containing the player's abilities information.
     */
    @Override
    public void onAbilities(@NotNull AbilitiesPacket packet) {
        // Update the player's abilities
        this.abilities.flying = packet.isFlying();
        this.abilities.allowFlight = packet.allowFlight();
        this.abilities.instaMine = packet.isInstaMine();
        this.abilities.invincible = packet.isInvincible();

        // Call superclass method
        super.onAbilities(packet);
    }

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

    @Override
    public void closeMenu() {
        super.closeMenu();

        if (this.client.connection != null)
            this.client.connection.send(new C2SCloseMenuPacket());
    }

    @Override
    public void openInventory() {
        super.openInventory();
        if (this.client.connection != null)
            this.client.connection.send(new C2SOpenInventoryPacket());
    }

    @Override
    public @NotNull WorldAccess getWorld() {
        return this.world;
    }

    public void onHealthUpdate(float newHealth) {
        this.oldHealth = this.health;
        this.health = newHealth;
    }

    public void resurrect() {
        this.setHealth(this.getMaxHealth());
        this.isDead = false;
    }

    public void onOpenMenu(MenuType<?> menuType, List<ItemStack> items) {
        ContainerMenu openedBefore = openMenu;
        if (openedBefore != null && menuType != openedBefore.getType()) {
            CommonConstants.LOGGER.warn("Opened menu {} instead of {}", menuType, openMenu);
        } else if (openedBefore == null) {
            CommonConstants.LOGGER.warn("Opened server menu {} before opening any on client side", menuType);
            if (this.world instanceof ClientWorld clientWorld) {
                openedBefore = menuType.create(clientWorld, this, this.getBlockVec());
            }
        }
        ContainerScreen screen = MenuRegistry.getScreen(openedBefore);
        screen.setup(items);
        this.client.showScreen(screen);
    }

    @Override
    public @NotNull Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return this.permissions.has(permission);
    }

    @Override
    public void dropItem() {
        this.client.connection.send(new C2SDropItemPacket());
    }

    public ClientPermissionMap getPermissions() {
        return this.permissions;
    }

    public void onHurt(S2CPlayerHurtPacket packet) {
        this.hurt(packet.getDamage(), packet.getSource());
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

    @Override
    @Deprecated
    public @NotNull Vec3d getPosition() {
        return super.getPosition();
    }
}
