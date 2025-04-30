package dev.ultreon.quantum.client;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.*;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.TimerInstance;
import dev.ultreon.quantum.TimerTask;
import dev.ultreon.quantum.client.world.ClientChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.sun.jdi.connect.spi.ClosedConnectionException;

import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.debug.BoxGizmo;
import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.gui.Notification;
import dev.ultreon.quantum.client.gui.icon.MessageIcon;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.network.MemoryConnectionContext;
import dev.ultreon.quantum.network.MemoryNetworker;
import dev.ultreon.quantum.network.packets.s2c.S2CPlayerSetPosPacket;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.ServerChunk;
import static dev.ultreon.quantum.world.World.CS;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.ubo.types.MapType;

/**
 * The IntegratedServer class represents a server integrated into the client system for local play.
 * This class provides various methods to manage the server, including starting it, loading players,
 * saving data, handling crashes, and more.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class IntegratedServer extends QuantumServer {
    private final QuantumClient client = QuantumClient.get();
    private boolean openToLan = false;
    private @Nullable ServerPlayer host;
    private final TimerInstance timer = GamePlatform.get().getTimer();

    /**
     * Constructs a new IntegratedServer with the given WorldStorage.
     *
     * @param storage The WorldStorage to use.
     * @throws RuntimeException If the world directory does not exist and cannot be created.
     */
    public IntegratedServer(WorldStorage storage) {
        super(storage, QuantumClient.PROFILER);

        // Check if the world directory exists.
        if (!storage.getDirectory().exists()) {
            try {
                // If the world directory does not exist, try to create it.
                storage.createWorld();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Starts the server.
     * <p>
     * This method starts the server by creating a new {@link MemoryNetworker} and calling the super method.
     * </p>
     * <p>
     * This method is called when the server is started.
     * </p>
     *
     * @see QuantumServer#start()
     * @see #start()
     * @see #shutdown()
     */
    @Override
    public void start() {
        this.networker = new MemoryNetworker(this, MemoryConnectionContext.get());
        super.start();
    }

    /**
     * Loads the given player into the server.
     *
     * @param localPlayer The local player to load.
     * @throws IllegalStateException If the player is not found.
     * @throws RuntimeException If an I/O error occurs during player data loading.
     */
    public void loadPlayer(@NotNull LocalPlayer localPlayer) {
        // Retrieve the player object from the server
        ServerPlayer player = this.getPlayer(localPlayer.getUuid());

        try {
            // Check if the player exists on the server and the player data file exists
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

    /**
     * Crashes the server.
     * <p>
     * This method crashes the server by shutting down the server and writing the crash log to the log file.
     * </p>
     * 
     */
    @Override
    public void crash(CrashLog crashLog) {
        this.shutdown();
        crashLog.writeToLog();

        client.delayCrash(crashLog);
    }

    /**
     * Called when the termination of the server fails.
     * <p>
     * This method is called when the termination of the server fails.
     * </p>
     */
    @Override
    protected void onTerminationFailed() {
        client.delayCrash(new CrashLog("onTerminationFailed", new Throwable("Failed termination of integrated server.")));
    }

    /**
     * Gets the render distance for the client.
     *
     * @return the render distance for the client.
     */
    @Override
    public int getRenderDistance() {
        return ClientConfiguration.renderDistance.getValue();
    }

    /**
     * Gets the render distance for entities.
     *
     * @return the render distance for entities.
     */
    @Override
    public int getEntityRenderDistance() {
        return ClientConfiguration.entityRenderDistance.getValue() / CS;
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
        this.client.remove(this);
        this.client.integratedServer = null;
    }

    @Override
    @SuppressWarnings("ConvertToTryWithResources")
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
    public void runTick() {
        this.client.pollServerTick();

        super.runTick();
    }

    @Override
    public UUID getHost() {
        ServerPlayer theHost = this.host;
        return theHost != null ? theHost.getUuid() : null;
    }

    @Override
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
            if (terrainRenderer instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) terrainRenderer;
                ClientChunk chunk = clientWorld.getChunk(builtChunk.vec);
                if (chunk == null) return;
                Gizmo gizmo = new BoxGizmo(chunk, "built_chunk_indicator", "built-chunk");
                gizmo.color.set(1F, 0.8F, 0F, 1F);
                gizmo.position.set(builtChunk.getOffset().vec().d().add(8, 8, 8));
                gizmo.size.set(15.5f, 15.5f, 15.5f);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new dev.ultreon.quantum.TimerTask() {
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
            if (terrainRenderer instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) terrainRenderer;
                ClientChunk chunk = clientWorld.getChunk(globalVec);
                if (chunk == null) return;
                Gizmo gizmo = new BoxGizmo(chunk, "request_chunk_indicator", "request-chunk");
                gizmo.color.set(0F, 0F, 1F, 1F);
                gizmo.position.set(globalVec.blockInWorldSpace(0, 0, 0).vec().d().add(8, 8, 8));
                gizmo.size.set(CS, CS, CS);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new dev.ultreon.quantum.TimerTask() {
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
            if (terrainRenderer instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) terrainRenderer;
                ClientChunk chunk = clientWorld.getChunk(serverChunk.vec);
                if (chunk == null) return;
                Gizmo gizmo = new BoxGizmo(chunk, "sent_chunk_indicator", "sent-chunk");
                gizmo.color.set(0F, 1F, 0F, 1F);
                gizmo.position.set(serverChunk.getOffset().vec().d().add(8, 8, 8));
                gizmo.size.set(15.5f, 15.5f, 15.5f);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new dev.ultreon.quantum.TimerTask() {
                    @Override
                    public void run() {
                        clientWorld.removeGizmo(gizmo);
                    }
                }, 10000L);
            }
        });
    }

    @Override
    public void addGizmo(BoundingBox boundingBox, Color color) {
        super.addGizmo(boundingBox, color);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess world = this.client.world;
            if (world instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) world;
                Gizmo gizmo = new BoxGizmo(Objects.requireNonNull(clientWorld), "unloaded_chunk_indicator", "unloaded-chunk");
                gizmo.color.set(1.0F, 0.5F, 1F, 1F);
                gizmo.position.set(boundingBox.min.x, boundingBox.min.y, boundingBox.min.z);
                gizmo.size.set((float) boundingBox.getWidth(), (float) boundingBox.getHeight(), (float) boundingBox.getDepth());
                gizmo.outline = false;

                clientWorld.addGizmo(gizmo);
            }
        });
    }

    @Override
    public void onChunkUnloaded(ServerChunk unloadedChunk) {
        super.onChunkUnloaded(unloadedChunk);

        QuantumClient.invoke(() -> {
            @Nullable ClientWorldAccess terrainRenderer = this.client.world;
            if (terrainRenderer instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) terrainRenderer;
                ClientChunk chunk = clientWorld.getChunk(unloadedChunk.vec);
                if (chunk == null) return;
                Gizmo gizmo = new BoxGizmo(chunk, "unloaded_chunk_indicator", "unloaded-chunk");
                gizmo.color.set(1.0F, 0.5F, 1F, 1F);
                gizmo.position.set(unloadedChunk.getOffset().vec().d().add(8, 8, 8));
                gizmo.size.set(15.5f, 15.5f, 15.5f);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new dev.ultreon.quantum.TimerTask() {
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
            if (terrainRenderer instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) terrainRenderer;
                Gizmo gizmo = new BoxGizmo(Objects.requireNonNull(clientWorld.getChunk(loadedChunk.vec)), "loaded_chunk_indicator", "loaded-chunk");
                gizmo.color.set(0F, 1.0F, 1F, 1F);
                gizmo.position.set(loadedChunk.getOffset().vec().d());
                gizmo.size.set(CS, CS, CS);
                gizmo.outline = true;
                clientWorld.addGizmo(gizmo);

                this.timer.schedule(new dev.ultreon.quantum.TimerTask() {
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
            if (terrainRenderer instanceof ClientWorld) {
                ClientWorld clientWorld = (ClientWorld) terrainRenderer;
                Gizmo gizmo = new BoxGizmo(Objects.requireNonNull(clientWorld), "failed_chunk_indicator", "failed-chunk");
                gizmo.color.set(1F, 0F, 1F, 1F);
                gizmo.position.set(d.add(8, 8, 8));
                gizmo.size.set(CS - 1, CS - 1, CS - 1);
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

    /**
     * Retrieves the {@link QuantumClient} instance associated with this server.
     *
     * @return the quantum voxel client instance.
     */
    public QuantumClient getClient() {
        return client;
    }
}
