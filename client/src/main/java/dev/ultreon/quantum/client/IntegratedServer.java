package dev.ultreon.quantum.client;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.debug.BoxGizmo;
import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.gui.Notification;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.network.MemoryConnectionContext;
import dev.ultreon.quantum.network.MemoryNetworker;
import dev.ultreon.quantum.network.packets.s2c.S2CPlayerSetPosPacket;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.ServerChunk;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Files;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class IntegratedServer extends QuantumServer {
    private final QuantumClient client = QuantumClient.get();
    private boolean openToLan = false;
    private @Nullable ServerPlayer host;
    private final Timer timer = new Timer();

    /**
     * Constructs a new IntegratedServer with the given WorldStorage.
     *
     * @param storage The WorldStorage to use.
     * @throws RuntimeException If the world directory does not exist and cannot be created.
     */
    public IntegratedServer(WorldStorage storage) {
        super(storage, QuantumClient.PROFILER, QuantumClient.get().inspection);

        // Check if the world directory exists.
        if (Files.notExists(storage.getDirectory())) {
            try {
                // If the world directory does not exist, try to create it.
                storage.createWorld();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void start() {
        this.networker = new MemoryNetworker(this, MemoryConnectionContext.get());

        super.start();
    }

    /**
     * Loads player data and sets the host player if found.
     *
     * @param localPlayer The local player object to load
     */
    public void loadPlayer(@NotNull LocalPlayer localPlayer) {
        // Retrieve the player object from the server
        ServerPlayer player = this.getPlayer(localPlayer.getUuid());

        try {
            // Check if the player exists on the server and player data file exists
            if (player != null && player.getUuid().equals(localPlayer.getUuid()) && this.getStorage().exists("player.ubo")) {
                // Load player data from storage
                var playerData = this.getStorage().<MapType>read("player.ubo");
                player.loadWithPos(playerData);

                // Send player a position update packet to player connection
                player.connection.send(new S2CPlayerSetPosPacket(player.getPosition()));
            } else if (player == null) {
                // Throw exception if player not found
                throw new IllegalStateException("Player not found.");
            }
        } catch (IOException e) {
            // Wrap and rethrow IOException as a RuntimeException
            throw new RuntimeException(e);
        }

        // Set the host player if the player UUID matches the local player UUID
        if (this.host == null && (player.getUuid().equals(localPlayer.getUuid()) || player.getName().equals(localPlayer.getName()))) {
            this.host = player;
        }

        // Create a debug node for host player if inspection is enabled
        if (DebugFlags.INSPECTION_ENABLED.isEnabled()) {
            this.node.createNode("host", () -> this.host);
        }
    }

    /**
     * Saves the player data to storage.
     */
    public void savePlayer() {
        // Get the player from the host
        ServerPlayer player = this.host;

        try {
            // Check if the player is not null
            if (player != null) {
                // Save the player data to a MapType object
                MapType save = player.save(new MapType());

                // Write the player data to storage
                this.getStorage().write(save, "player.ubo");

                // Log that the player data has been saved
                QuantumServer.LOGGER.info("Saved local player data.");
            } else {
                // Log an error if player is not found
                QuantumServer.LOGGER.error("Player not found.");
            }
        } catch (IOException e) {
            // Throw a runtime exception if there is an IOException
            throw new RuntimeException(e);
        }
    }

    @Override
    public void crash(CrashLog crashLog) {
        this.shutdown();
        crashLog.writeToLog();

        this.client.showScreen(new CrashScreen(List.of(crashLog)));
    }

    @Override
    protected void onTerminationFailed() {
        client.delayCrash(new CrashLog("onTerminationFailed", new Throwable("Failed termination of integrated server.")));
    }

    @Override
    public int getRenderDistance() {
        return ClientConfig.renderDistance;
    }

    /**
     * Places the player on the server.
     * <p>Called when the player joins the server.</p>
     *
     * @param player the player to place.
     */
    @Override
    public void placePlayer(ServerPlayer player) {
        this.deferWorldLoad(() -> {
            super.placePlayer(player);

            LocalPlayer localPlayer = this.client.player;
            if (localPlayer != null && player.getUuid().equals(localPlayer.getUuid())) {
                this.client.integratedServer.loadPlayer(localPlayer);
            }
        });
    }

    /**
     * Defer world load.
     * <p>Useful for when the world is not loaded yet.</p>
     *
     * @param func the function to defer.
     */
    private void deferWorldLoad(Runnable func) {
        this.client.runInTick(func);
    }

    /**
     * Check if the server is integrated.
     * Which is true for the {@link IntegratedServer}.
     *
     * @return true if the server is integrated.
     */
    @Override
    public boolean isIntegrated() {
        return true;
    }

    /**
     * Check if the server is opened to LAN.
     * <p>
     * NOTE: This is an experimental feature.
     *
     * @return true if the server is opened to LAN.
     * @see #openToLan()
     */
    @ApiStatus.Experimental
    public boolean isOpenToLan() {
        return this.openToLan;
    }

    /**
     * Opens the server to LAN.
     * Open to LAN is a server that can be accessed from the local network.
     * This is also hosted locally by the {@link QuantumClient client}.
     * <p>
     * NOTE: This is an experimental feature.
     *
     * @see #isOpenToLan()
     */
    @ApiStatus.Experimental
    public void openToLan() {
        this.openToLan = true;
    }

    /**
     * Save the world to disk.
     *
     * @param silent true to silence the logs.
     * @throws IOException when saving the world fails.
     */
    @Override
    public void save(boolean silent) throws IOException {
        try {
            // Normal server saving.
            super.save(silent);
        } catch (IOException e) {
            QuantumServer.LOGGER.error("Failed to save world", e);
        }

        try {
            // Save player data.
            this.savePlayer();
        } catch (Exception e) {
            QuantumServer.LOGGER.error("Failed to save local player data.", e);
        }
    }

    @Override
    public String toString() {
        return "IntegratedServer{" +
                "openToLan=" + this.openToLan +
                '}';
    }

    @Override
    public void shutdown() {
        super.shutdown();

        this.timer.cancel();
        this.client.integratedServer = null;
    }

    @Override
    public void close() {
        this.timer.cancel();

        super.close();

        try {
            this.getNetworker().close();
        } catch (ClosedChannelException | ClosedConnectionException e) {
            // Ignore
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void runTick() {
        this.client.pollServerTick();

        super.runTick();
    }

    @Override
    public UUID getHost() {
        return this.host != null ? this.host.getUuid() : null;
    }

    @Override
    @InternalApi
    @ApiStatus.Internal
    public void handleWorldSaveError(Exception e) {
        super.handleWorldSaveError(e);

        this.client.notifications.add(Notification.builder("Error", "Failed to save world.")
                .subText("Auto Save")
                .icon(MessageIcon.ERROR)
                .build());
    }

    @Override
    public void handleChunkLoadFailure(ChunkVec globalVec, String reason) {
        super.handleChunkLoadFailure(globalVec, reason);

        this.client.notifications.add(Notification.builder("Failed to load: " + globalVec.toString(), reason)
                .subText("Chunk Loader")
                .icon(MessageIcon.WARNING)
                .build());
    }

    @Override
    public void fatalCrash(Throwable throwable) {
        QuantumClient.crash(throwable);
    }

    @Override
    public @NotNull List<Runnable> shutdownNow() {
        this.timer.cancel();

        return super.shutdownNow();
    }

    @Override
    public void onChunkBuilt(ServerChunk builtChunk) {
        super.onChunkBuilt(builtChunk);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess terrainRenderer = this.client.world;
            if (terrainRenderer instanceof ClientWorld clientWorld) {
                Gizmo gizmo = new BoxGizmo("built-chunk");
                gizmo.color.set(1F, 0.8F, 0F, 1F);
                gizmo.position.set(builtChunk.getOffset().vec().d().add(8, 8, 8));
                gizmo.size.set(15.5f, 15.5f, 15.5f);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        clientWorld.removeGizmo(gizmo);
                    }
                }, 10000L);
            }
        });
    }

    @Override
    public void onChunkLoadRequested(ChunkVec globalVec) {
        super.onChunkLoadRequested(globalVec);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess terrainRenderer = this.client.world;
            if (terrainRenderer instanceof ClientWorld clientWorld) {
                Gizmo gizmo = new BoxGizmo("request-chunk");
                gizmo.color.set(0F, 0F, 1F, 1F);
                gizmo.position.set(globalVec.blockInWorldSpace(0, 0, 0).vec().d().add(8, 8, 8));
                gizmo.size.set(16, 16, 16);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        clientWorld.removeGizmo(gizmo);
                    }
                }, 10000L);
            }
        });
    }

    @Override
    public void onChunkSent(ServerChunk serverChunk) {
        super.onChunkSent(serverChunk);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess terrainRenderer = this.client.world;
            if (terrainRenderer instanceof ClientWorld clientWorld) {
                Gizmo gizmo = new BoxGizmo("sent-chunk");
                gizmo.color.set(0F, 1F, 0F, 1F);
                gizmo.position.set(serverChunk.getOffset().vec().d().add(8, 8, 8));
                gizmo.size.set(15.5f, 15.5f, 15.5f);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        clientWorld.removeGizmo(gizmo);
                    }
                }, 10000L);
            }
        });
    }

    @Override
    public void onChunkUnloaded(ServerChunk unloadedChunk) {
        super.onChunkUnloaded(unloadedChunk);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess terrainRenderer = this.client.world;
            if (terrainRenderer instanceof ClientWorld clientWorld) {
                Gizmo gizmo = new BoxGizmo("unloaded-chunk");
                gizmo.color.set(1.0F, 0.5F, 1F, 1F);
                gizmo.position.set(unloadedChunk.getOffset().vec().d().add(8, 8, 8));
                gizmo.size.set(15.5f, 15.5f, 15.5f);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        clientWorld.removeGizmo(gizmo);
                    }
                }, 10000L);
            }
        });
    }

    @Override
    public void onChunkLoaded(ServerChunk loadedChunk) {
        super.onChunkLoaded(loadedChunk);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess terrainRenderer = this.client.world;
            if (terrainRenderer instanceof ClientWorld clientWorld) {
                Gizmo gizmo = new BoxGizmo("loaded-chunk");
                gizmo.color.set(0F, 1.0F, 1F, 1F);
                gizmo.position.set(loadedChunk.getOffset().vec().d());
                gizmo.size.set(16, 16, 16);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        clientWorld.removeGizmo(gizmo);
                    }
                }, 10000L);
            }
        });
    }

    @Override
    public void onChunkFailedToLoad(Vec3d d) {
        super.onChunkFailedToLoad(d);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess terrainRenderer = this.client.world;
            if (terrainRenderer instanceof ClientWorld clientWorld) {
                Gizmo gizmo = new BoxGizmo("failed-chunk");
                gizmo.color.set(1F, 0F, 1F, 1F);
                gizmo.position.set(d.add(8, 8, 8));
                gizmo.size.set(15, 15, 15);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        clientWorld.removeGizmo(gizmo);
                    }
                }, 10000L);
            }
        });
    }

    public QuantumClient getClient() {
        return this.client;
    }
}
