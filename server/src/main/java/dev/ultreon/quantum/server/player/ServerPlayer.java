package dev.ultreon.quantum.server.player;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.DoNotCall;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.api.commands.Command;
import dev.ultreon.quantum.api.commands.CommandContext;
import dev.ultreon.quantum.api.commands.TabCompleting;
import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.events.MenuEvents;
import dev.ultreon.quantum.events.PlayerEvents;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.AbilitiesPacket;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.s2c.*;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.registry.CommandRegistry;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.chat.Chat;
import dev.ultreon.quantum.text.Formatter;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.ubo.types.MapType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Server-side player implementation.
 * Represents an online player.
 * <p style="color: red;">NOTE: Should not be stored in collections, if you need to store a player in a collection. You should get the cached player instead,</p>
 *
 * @see QuantumServer#getCachedPlayer(String)
 */
public class ServerPlayer extends Player implements CacheablePlayer {
    private final Vec3d tmp3d$1 = new Vec3d();
    public IConnection<ServerPacketHandler, ClientPacketHandler> connection;
    private ServerWorld world;
    public int hotbarIdx;
    private final UUID uuid;
    private final String name;
    private final QuantumServer server = QuantumServer.get();
    public boolean blockBrokenTick = false;
    private boolean sendingChunk;
    private boolean spawned;
    private boolean playedBefore;
    private final MutablePermissionMap permissions = new MutablePermissionMap();
    private boolean isAdmin;
    private final Tracker<ChunkVec> chunkTracker = new Tracker<>();
    private boolean isInactive;
    private final Vec3d tmp3Da = new Vec3d();

    public ServerPlayer(EntityType<? extends Player> entityType, ServerWorld world, UUID uuid, String name, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        super(entityType, world, name);
        this.world = world;
        this.uuid = uuid;
        this.name = name;

        this.connection = connection;

        this.permissions.allows.add(new Permission("*")); // FIXME: Allow custom default permissions.
    }

    /**
     * Kicks the player with the given message.
     *
     * @param message the kick message.
     */
    public void kick(String message) {
        if (this.connection == null) return;
        this.connection.disconnect(message);
    }

    /**
     * Kicks the player with the given message as {@link TextObject}.
     *
     * @param message the kick message.
     */
    public void kick(TextObject message) {
        this.connection.disconnect(message);
    }

    /**
     * Respawn the player in the world.
     */
    public void respawn() {
        // Check if the world is not null
        assert this.world != null;

        try {
            // Get the spawn point for the player
            BlockVec spawnPoint = this.server.submit(this.world::getSpawnPoint).join();

            // Calculate the spawn position
            Vec3d spawnAt = spawnPoint.vec().d().add(0.5, 0, 0.5);

            // Set player's position, health, and status
            this.setPosition(spawnAt);
            this.health = this.getMaxHealth();
            this.getFoodStatus().reset();
            this.isDead = false;
            this.damageImmunity = 40;

            // Spawn player at the calculated spawn point
            this.spawn(spawnAt, this.connection);
        } catch (Exception e) {
            // Log error if failed to spawn player
            QuantumServer.LOGGER.error("Failed to spawn player!", e);
        }
    }

    /**
     * Called when the player takes damage.
     *
     * @param damage the damage dealt.
     * @param source the source of the damage.
     * @return true to cancel the damage.
     */
    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        // Check if the damage immunity is active
        if (this.damageImmunity > 0) return true;

        // Call the superclass to handle the damage as player/living entity. To get if the damage was canceled.
        boolean noDamage = super.onHurt(damage, source);

        if (!noDamage) {
            // Play hurt sound
            this.playSound(this.getHurtSound(), 1.0f);
            this.connection.send(new S2CPlayerHurtPacket(damage, source));
        }
        return noDamage;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);

        // Remove the player from the world if it exists in the world
        if (this.world.getEntity(this.getId()) == this) {
            this.world.despawn(this);
        }

    }

    /**
     * Spawns the entity at the specified position with the given connection.
     *
     * @param position   The position to spawn the entity
     * @param connection The connection used for spawning
     */
    @ApiStatus.Internal
    public void spawn(Vec3d position, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        Preconditions.checkNotNull(position, "position");
        Preconditions.checkNotNull(connection, "connection");

        // Set the entity's connection and health, and position it at the specified position
        this.connection = connection;
        this.setHealth(this.getMaxHealth());
        this.setPosition(position);

        // Prepare and spawn the entity in the world
        this.world.prepareSpawn(this);
        this.world.spawn(this);

        this.sendRecipes();

        // Send gamemode and respawn packets to the connection
        this.connection.send(new S2CGamemodePacket(this.getGamemode()));
        this.connection.send(new S2CRespawnPacket(this.getPosition()));

        // Mark the entity as spawned
        this.spawned = true;
    }

    @ApiStatus.Internal
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void sendRecipes() {
        for (RecipeType<?> type : Registries.RECIPE_TYPE.values()) {
            this.connection.send(new S2CRecipeSyncPacket(type, this.world.getServer().getRecipeManager().getRegistry(type)));
        }
    }

    public void markSpawned() {
        this.spawned = true;
    }

    /**
     * This method is called every tick to update the player's state.
     */
    @Override
    public void tick() {
        // Reset the blockBrokenTick flag
        this.blockBrokenTick = false;

        // Call the superclass tick method
        super.tick();

        // Move the player
        move(velocityX, velocityY, velocityZ);

        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;


        if (this.world.isServerSide()) {
            float curSpeed = (float) this.tmp3Da.set(this.ox, this.oy, this.oz).dst(this.x, this.y, this.z);

            if (curSpeed > 0) {
                getFoodStatus().exhaust(curSpeed / 10f);
            }
        }

        // Check if the player's health has changed
        if (this.oldHealth != this.health) {
            // Send the updated health to the client
            this.connection.send(new S2CPlayerHealthPacket(this.health));
            // Update the old health value
            this.oldHealth = this.health;
        }

        // Get the currently open menu
        ContainerMenu menu = this.getOpenMenu();
        if (menu != null) {
            // Get the position of the menu
            BlockVec pos = menu.getPos();
            // Check if the distance between the player and the menu position is greater than 5
            if (pos != null && pos.vec().d().dst(this.getPosition()) > 5)
                // Auto-close the menu if the distance is greater than 5
                this.autoCloseMenu();
        }
    }

    @Override
    public void load(@NotNull MapType data) {
        super.load(data);

        if (connection != null && !connection.isLoggingIn()) sendAllData();
    }

    /**
     * Sends all data to the client.
     *
     * @apiNote Should only be called when strictly necessary.
     * Sending all data too frequently can cause lag and too much unnecessary network traffic.
     */
    @ApiStatus.Internal
    public void sendAllData() {
        Vec3d position = getPosition();
        connection.send(new S2CPlayerPositionPacket(uuid, position, xHeadRot, xRot, yRot));
        connection.send(new S2CPlayerHealthPacket(health));
        connection.send(new S2CGamemodePacket(getGamemode()));
        connection.send(new S2CTemperatureSyncPacket(getTemperature()));
        inventory.onAllChanged();
        sendAbilities();
    }

    @Override
    protected void tickTemperature() {
        super.tickTemperature();

        if (getAge() % 10 == 0) {
            sendPacket(new S2CTemperatureSyncPacket(getTemperature()));
        }
    }

    private void autoCloseMenu() {
        this.connection.send(new S2CCloseMenuPacket());
    }

    @Override
    protected void move() {

    }

    /**
     * {@inheritDoc}
     * <p>
     * Also updates player old positions and send packets to nearby players.
     */
    @Override
    protected void onMoved() {
        // Check if the chunk is loaded and the entity is in an active chunk
        if (world.getChunkNoLoad(this.getChunkVec()) == null) {
            isInactive = true;
            return;
        }
        if (!this.isChunkActive(this.getChunkVec())) {
            isInactive = true;
            return;
        }

        if (isInactive) {
            connection.send(new S2CPlayerPositionPacket(this.getUuid(), this.getPosition(), xHeadRot, xRot, yRot));
            isInactive = false;
        }

        super.onMoved();

        // Set old position.
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;

        // Send position update packets to nearby players
        for (ServerPlayer player : this.server.getPlayers()) {
            if (player == this) continue;

            // Check if player is within entity render distance
            if (player.getPosition().dst(this.getPosition()) < this.server.getEntityRenderDistance())
                player.connection.send(new S2CPlayerPositionPacket(this.getUuid(), this.getPosition(), xHeadRot, xRot, yRot));
        }
    }

    @Override
    public void teleportTo(int x, int y, int z) {
        super.teleportTo(x, y, z);

        this.connection.send(new S2CPlayerSetPosPacket(x + 0.5, y, z + 0.5));
    }

    @Override
    public void teleportTo(double x, double y, double z) {
        super.teleportTo(x, y, z);

        this.connection.send(new S2CPlayerSetPosPacket(x, y, z));
    }

    @Override
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isCache() {
        return false;
    }

    @Override
    public @NotNull Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "ServerPlayer{'" + this.name + "' : " + this.getUuid() + "}";
    }

    /**
     * Called when a chunk is loaded, unloaded, or failed to load.
     *
     * @param vec    the position of the chunk
     * @param status the status of the chunk
     */
    public void onChunkStatus(@NotNull ChunkVec vec, Chunk.Status status) {
        switch (status) {
            case FAILED -> this.handleFailedChunk(vec);
            case UNLOADED -> {
                ServerChunk chunk = this.world.getChunkNoLoad(vec);
                if (chunk != null) {
                    chunk.getTracker().stopTracking(this);
                }
                this.chunkTracker.stopTracking(vec);
            }
            case SUCCESS -> this.handleClientLoadChunk(vec);
        }

        // Handle chunk load failure if the status is failed
        if (status == Chunk.Status.FAILED)
            this.server.handleChunkLoadFailure(vec, "Chunk failed to load on client.");
    }

    public void sendPacket(Packet<? extends ClientPacketHandler> packet) {
        try {
            this.connection.send(packet);
        } catch (Exception e) {
            this.connection.disconnect("Internal server error");
            this.connection.on3rdPartyDisconnect("Internal server error");
            throw new RuntimeException(e);
        }
    }

    private void handleClientLoadChunk(@NotNull ChunkVec vec) {
        // Handle the chunk status accordingly
        if (vec.dst(this.getChunkVec()) * World.CHUNK_SIZE > this.server.getRenderDistance()) {
            this.sendPacket(new S2CChunkUnloadPacket(vec));
            return;
        }

        if (tmp3d$1.set(this.ox, this.oy, this.oz).dst(this.getPosition()) > 5) {
            this.teleportTo(this.ox, this.oy, this.oz);
            this.sendMessage("[light red]You moved to quickly!");
            return;
        }

        if (DebugFlags.LOG_POSITION_RESET_ON_CHUNK_LOAD.isEnabled())
            Chat.sendInfo(this, "Position reset on chunk load.");

        ServerChunk chunk = this.world.getChunk(vec);
        chunk.getTracker().startTracking(this);

        this.chunkTracker.startTracking(vec);
    }

    private void handleFailedChunk(@NotNull ChunkVec vec) {
        if (DebugFlags.LOG_CHUNK_LOAD_FAILURE.isEnabled())
            Chat.sendInfo(this, "Failed to load chunk " + vec);

        ServerChunk chunk = this.world.getChunk(vec);
        if (chunk != null) {
            chunk.getTracker().stopTracking(this);
            this.chunkTracker.stopTracking(vec);

            this.connection.send(new S2CChunkUnloadPacket(vec));
        }
    }

    /**
     * Send a chunk to the client.
     *
     * @param vec   The position of the chunk.
     * @param chunk The chunk to send.
     */
    public void sendChunk(@NotNull ChunkVec vec, @NotNull ServerChunk chunk) {
        if (this.sendingChunk) return;

        this.connection.send(new S2CChunkDataPacket(vec, chunk.info, chunk.storage.clone(), chunk.biomeStorage.clone(), chunk.getBlockEntities()));
        this.sendingChunk = false;
    }

    /**
     * {@inheritDoc}
     * To play the sound, this will send a {@link S2CPlaySoundPacket} over to the client.
     *
     * @param sound  The sound event to be played. Can be null.
     * @param volume The volume at which the sound should be played.
     */
    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        if (sound == null) return;
        this.connection.send(new S2CPlaySoundPacket(sound.getId(), volume));
    }

    /**
     * Send the player's abilities to the client.
     */
    @Override
    protected void sendAbilities() {
        this.connection.send(new S2CAbilitiesPacket(this.abilities));
    }

    /**
     * Handles the AbilitiesPacket received from the client.
     * If the player is trying to fly when the ability to flight is not allowed, disconnects them.
     *
     * @param packet The AbilitiesPacket received from the client.
     */
    @Override
    public void onAbilities(@NotNull AbilitiesPacket packet) {
        // Check if the player is trying to fly
        boolean flying = packet.flying();
        // Check if flight is allowed
        boolean allowFlight = this.abilities.allowFlight;

        // If the player is trying to fly and flight is not allowed, disconnect them
        if (flying && !allowFlight) {
            this.connection.disconnect("Kicked for flying.");
            return;
        }

        // Call the superclass method to handle the AbilitiesPacket
        super.onAbilities(packet);

        // Update the flying status of the player
        this.abilities.flying = flying;
    }

    /**
     * Override method to open a menu.
     * If the menu open event is not canceled, opens the menu by sending a packet.
     *
     * @param menu The menu to be opened.
     */
    @Override
    public void openMenu(@NotNull ContainerMenu menu) {
        if (getOpenMenu() == menu) {
            QuantumServer.LOGGER.warn("Player {} tried to open menu {} but it was already open!", this.name, menu.getType().getId());
            return;
        }

        // Check if the menu open event is canceled, if so, return early
        if (MenuEvents.MENU_OPEN.factory().onMenuOpen(menu, this).isCanceled())
            return;

        // Call the superclass meth\od to open the menu
        super.openMenu(menu);

        // Send a packet to open the container menu
        this.connection.send(S2COpenMenuPacket.of(menu.getType().getId(), Arrays.asList(menu.slots)));
    }

    @Override
    public void setCursor(@NotNull ItemStack cursor) {
        this.connection.send(new S2CMenuCursorPacket(cursor));
        super.setCursor(cursor);
    }

    /**
     * Sets the gamemode and sends relevant packets if the gamemode has changed.
     *
     * @param gamemode the new gamemode to set
     */
    @Override
    public void setGameMode(@NotNull GameMode gamemode) {
        GameMode old = this.getGamemode();
        super.setGameMode(gamemode);

        // If the gamemode has changed, send relevant packets
        if (old != gamemode) {
            this.connection.send(new S2CGamemodePacket(gamemode));

            // Set the abilities of the player and send them to the client
            gamemode.setAbilities(this.abilities);
            this.connection.send(new S2CAbilitiesPacket(this.abilities));
        }
    }

    @Override
    public @NotNull ServerWorld getWorld() {
        return this.world;
    }

    public boolean isChunkActive(ChunkVec chunkVec) {
        return this.chunkTracker.isTracking(chunkVec);
    }

    /**
     * Sets the initial items of the player.
     */
    public void setInitialItems() {
        // Check if the event for initial items is canceled
        if (PlayerEvents.INITIAL_ITEMS.factory().onPlayerInitialItems(this, this.inventory).isCanceled()) {
            return;
        }
        // Add initial items to the player's inventory
        this.inventory.addItem(Items.WOODEN_PICKAXE.defaultStack());
        this.inventory.addItem(Items.WOODEN_SHOVEL.defaultStack());
        this.inventory.addItem(new ItemStack(Items.CRATE, 32));
        this.inventory.addItem(new ItemStack(Items.BLAST_FURNACE, 32));
        this.inventory.addItem(new ItemStack(Items.BACON, 64));
    }

    /**
     * Handles player movement from the client.
     *
     * @param x the x-coordinate received from the client
     * @param y the y-coordinate received from the client
     * @param z the z-coordinate received from the client
     */
    public void handlePlayerMove(double x, double y, double z) {
        ChunkVec chunkVec = new BlockVec(x, y, z, BlockVecSpace.WORLD).chunk();
        ServerChunk chunk = this.world.getChunkNoLoad(chunkVec);
        if (chunk == null) {
            return;
        }
        if (!chunk.getTracker().isTracking(this)) {
            QuantumServer.LOGGER.warn("Player moved into an untracked chunk: {}", this.getName());
            return;
        }


        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Handles player movement from the client.
     *
     * @param x the x-coordinate received from the client
     * @param y the y-coordinate received from the client
     * @param z the z-coordinate received from the client
     */
    public void handlePlayerMove(double x, double y, double z, float xHeadRot, float xRot, float yRot) {
        ChunkVec chunkVec = new BlockVec(x, y, z, BlockVecSpace.WORLD).chunk();
        ServerChunk chunk = this.world.getChunkNoLoad(chunkVec);
        if (chunk == null) {
            return;
        }
        if (!chunk.getTracker().isTracking(this)) {
            QuantumServer.LOGGER.warn("Player moved into an inactive chunk: {}", this.getName());
            return;
        }

        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;

        this.x = x;
        this.y = y;
        this.z = z;

        this.xHeadRot = xHeadRot;
        this.xRot = xRot;
        this.yRot = yRot;

        this.world.sendAllTrackingExcept((int) x, (int) y, (int) z, new S2CPlayerPositionPacket(this.getUuid(), new Vec3d(x, y, z), xHeadRot, xRot, yRot), this);
    }

    public boolean isSpawned() {
        return this.spawned;
    }

    public void markPlayedBefore() {
        this.playedBefore = true;
    }

    public boolean hasPlayedBefore() {
        return this.playedBefore;
    }

    public void tabComplete(String input) {
        if (input.startsWith("/")) {
            input = input.substring(1);
            if (!input.contains(" ")) {
                this.connection.send(new S2CTabCompletePacket(TabCompleting.commands(new ArrayList<>(), input)));
                return;
            }

            String command;
            String[] argv;
            argv = input.split(" ");
            command = argv[0];
            argv = ArrayUtils.remove(argv, 0);

            Command baseCommand = CommandRegistry.get(command);
            if (baseCommand == null) {
                return;
            }

            if (input.endsWith(" ")) {
                argv = ArrayUtils.add(argv, "");
            }

            List<String> options = baseCommand.onTabComplete(this, new CommandContext(command), command, argv);
            if (options == null) options = Collections.emptyList();
            this.connection.send(new S2CTabCompletePacket(options));
        }
    }

    public void onMessageSent(String message) {
        for (ServerPlayer player : this.server.getPlayers()) {
            player.sendMessage(new Formatter(true, true, "[cyan]&<" + this.getName() + "> [white]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.WHITE).parse().getResult());
        }
    }

    @Override
    public void sendMessage(@NotNull TextObject textObj) {
        this.connection.send(new S2CChatPacket(textObj));
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.sendMessage(new Formatter(true, true, message, TextObject.empty(), TextObject.empty(), null, RgbColor.WHITE).parse().getResult());
    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return this.permissions.has(permission);
    }

    @Override
    public boolean isAdmin() {
        return this.isAdmin;
    }

    @Override
    @DoNotCall
    public void onTeleportedDimension(@NotNull WorldAccess world) {
        super.onTeleportedDimension(world);

        if (!(world instanceof ServerWorld serverWorld)) throw new IllegalArgumentException("Expected a " + ServerWorld.class.getName() +", got a " + world.getClass().getName());
        this.world = serverWorld;
        this.sendPacket(new S2CChangeDimensionPacket(world.getDimension()));
    }

    public void makeAdmin() {
        this.isAdmin = true;
        this.resendCommands();
    }

    public void resendCommands() {
        this.connection.send(new S2CCommandSyncPacket(CommandRegistry.getCommandNames().collect(Collectors.toList())));
    }

    public UseResult useItem(BlockHit hitResult, ItemStack stack, ItemSlot slot) {
        UseItemContext ctx = new UseItemContext(getWorld(), this, hitResult, stack, 1F);
        BlockHit result = (BlockHit) ctx.result();
        if (result == null)
            return UseResult.SKIP;

        Block block = result.getBlock();
        if (block != null && !block.isAir()) {
            UseResult blockResult = block.use(ctx.world(), ctx.player(), stack.getItem(), new BlockVec(result.getBlockVec()));

            if (blockResult == UseResult.DENY || blockResult == UseResult.ALLOW)
                return blockResult;
        }

        UseResult itemResult = stack.getItem().use(ctx);
        if (itemResult == UseResult.DENY)
            slot.update();

        return itemResult;
    }

    /**
     * Called when the player places a block on the client side.
     * This is called from the packet when handled.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param block the block to place
     */
    public void placeBlock(int x, int y, int z, BlockState block) {
        BlockVec blockVec = new BlockVec(x, y, z, BlockVecSpace.WORLD);
        if (block == null || block.isAir() || !this.world.isLoaded(blockVec) || !this.world.get(x, y, z).isAir()) {
            this.world.sync(blockVec);
            return;
        }

        this.world.set(x, y, z, block, BlockFlags.SYNC | BlockFlags.UPDATE);
    }

    public void onDisconnect(String message) {
        QuantumServer.LOGGER.info("Player {} disconnected: {}", this.getName(), message);
    }

    public void onAttack(int id) {
        Entity entity = this.world.getEntity(id);
        if (entity == null) return;
        this.world.sendAllTrackingExcept((int) entity.getX(), (int) entity.getY(), (int) entity.getZ(), new S2CPlayerAttackPacket(this.getId(), id), this);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.hurt(this.getAttackDamage(), DamageSource.ATTACK);
        }
    }

    public float getAttackDamage() {
        ItemStack selectedItem = this.getSelectedItem();
        return selectedItem.getAttackDamage() + 1f;
    }

    public void requestChunkLoad(ChunkVec pos) {

        // Get or load the chunk.
        synchronized (this.chunkTracker) {
            QuantumServer.invoke(() -> {
                try {
                    this.server.onChunkLoadRequested(pos);
                    if (!this.chunkTracker.isTracking(pos)) {
                        this.chunkTracker.startTracking(pos);
                    } else {
                        ServerChunk chunk = this.world.getChunk(pos);
                        chunk.sendChunk();
                    }

                    this.world.getOrLoadChunk(pos).thenAccept(receivedChunk -> {
                        if (receivedChunk == null) {
                            this.server.onChunkFailedToLoad(pos.blockInWorldSpace(0, 0, 0).vec().d());
                            return;
                        }

                        receivedChunk.getTracker().startTracking(this);
                        QuantumServer.invoke(receivedChunk::sendChunk);
                    }).exceptionally(throwable -> {
                        this.chunkTracker.stopTracking(pos);
                        this.sendPacket(new S2CChunkUnloadPacket(pos));

                        this.server.onChunkFailedToLoad(pos.blockInWorldSpace(0, 0, 0).vec().d());

                        CommonConstants.LOGGER.error("Failed to load chunk {} due to exception", pos, throwable);

                        return null;
                    });
                } catch (Exception e) {
                    this.chunkTracker.stopTracking(pos);
                    this.sendPacket(new S2CChunkUnloadPacket(pos));
                    this.server.onChunkFailedToLoad(pos.blockInWorldSpace(0, 0, 0).vec().d());
                    CommonConstants.LOGGER.error("Failed to load chunk {} due to exception", pos, e);
                }
            });
        }
    }

    public void stopTracking(ChunkVec vec) {
        synchronized (this.chunkTracker) {
            this.chunkTracker.stopTracking(vec);

            this.world.stopTrackingChunk(vec, this);
        }
    }
}
