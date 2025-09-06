package dev.ultreon.quantum.client.gui.screens.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.TimerTask;
import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.WorldDataCorruptionError;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.screens.DeathScreen;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.render.world.WorldRenderer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.WorldStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;

public class WorldLoadScreen extends Screen {
    public static final @NotNull Color PROGRESS_BG = new Color(0xffffff80);
    public static final @NotNull Color PROGRESS_FG = new Color(0xff0040ff);
    private Label titleLabel;
    private Label descriptionLabel;
    private Label subTitleLabel;
    private final WorldStorage storage;
    private long nextLog;
    private ServerWorld world;
    private DeathScreen closeScreen;
    private boolean done = false;
    private volatile boolean loggedIn;
    private int chunksToLoadCount;

    public WorldLoadScreen(WorldStorage storage) {
        super(TextObject.translation("quantum.screen.world_load"));
        this.storage = storage;
    }

    @Override
    protected void init() {
        super.init();

        final IntegratedServer server;
        try {
            server = new IntegratedServer(this.storage);
        } catch (IOException e) {
            client.showScreen(new DisconnectedScreen("Server failed to initialize!", false));
            return;
        }
        this.client.integratedServer = server;

        this.client.add("Integrated Server", server);

        try {
            server.init();
        } catch (WorldDataCorruptionError e) {
            QuantumClient.LOGGER.error("Failed to initialize server:", e);
            server.shutdown(() -> {
                QuantumClient.LOGGER.error("World data corrupted!");
                this.client.remove(server);
                client.showScreen(new DisconnectedScreen("World data corrupted!", false));
            });
            return;
        } catch (IOException e) {
            QuantumClient.LOGGER.error("Failed to initialize server:", e);
            this.client.remove(server);
            client.showScreen(new DisconnectedScreen("Server failed to initialize!", false));
            server.shutdown(() -> {
            });
            return;
        }

        this.world = server.getDimManager().getWorld(DimensionInfo.OVERWORLD);
        this.client.openedWorld = this.storage;

        this.titleLabel = Label.of(this.title)
                .withAlignment(Alignment.CENTER)
                .withScale(2);

        this.descriptionLabel = Label.of("Preparing")
                .withAlignment(Alignment.CENTER);

        this.subTitleLabel = Label.of()
                .withAlignment(Alignment.CENTER);

        GamePlatform.get().runAsync(this::run);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        Label titleLabel1 = this.titleLabel;
        if (titleLabel1 != null)
            titleLabel1.setPos(this.size.width / 2, this.size.height / 3 - 25);
        Label descriptionLabel1 = this.descriptionLabel;
        if (descriptionLabel1 != null)
            descriptionLabel1.setPos(this.size.width / 2, this.size.height / 3 + 3);
        Label subTitleLabel1 = this.subTitleLabel;
        if (subTitleLabel1 != null)
            subTitleLabel1.setPos(this.size.width / 2, this.size.height / 3 + 31);
    }

    @Override
    public boolean onClose(@Nullable Screen next) {
        DeathScreen closeScreen = this.closeScreen;
        if (next == null && closeScreen != null) {
            this.client.showScreen(closeScreen);
        }

        return super.onClose(next);
    }

    public void run() {
        try {
            assert this.world != null;
            MathUtils.random.setSeed(this.world.getSeed());

            this.message("Starting integrated server...");
            this.client.startIntegratedServer();

            this.message("Loading saved world...");
            if (this.loadGeneric()) return;

            this.message("Set spawn point");

            this.world.setupSpawn();

            waitUntilLoggedIn();
        } catch (Exception throwable) {
            QuantumClient.LOGGER.error("Failed to load world:", throwable);
            QuantumClient.crash(throwable);
        }
    }

    private void waitUntilLoggedIn() {
        var connection = client.connection;
        if (loggedIn && client.player != null && connection != null && connection.isConnected()) {
            completeRun();
        } else {
            GamePlatform.get().getTimer().schedule(new TimerTask() {
                @Override
                public void run() {
                    waitUntilLoggedIn();
                }
            }, 100);
        }
    }

    private void completeRun() {
        try {
            this.message("Waiting for server to finalize...");
        } catch (Exception throwable) {
            QuantumClient.LOGGER.error("Failed to load world:", throwable);
            QuantumClient.crash(throwable);
        }
    }

    private boolean loadGeneric() {
        try {
            this.client.integratedServer.load();
            this.message("Saved world loaded!");
        } catch (IOException e) {
            QuantumClient.crash(e);
            return true;
        }
        return false;
    }

    private void message(String message) {
        this.descriptionLabel.text().setRaw(message);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        this.renderSolidBackground(renderer);

        ServerWorld world = this.world;
        if (world != null) {
            int chunksToLoad = chunksToLoadCount;
            if (chunksToLoad == -1) {
                this.subTitleLabel.text().set(TextObject.translation(this.client.integratedServer == null ? "quantum.screen.worldLoad.enteringWorld" : "quantum.screen.worldLoad.enteringServer"));
            } else if (chunksToLoad != 0) {
                ClientWorld worldAccess = this.client.world;
                if (worldAccess == null) {
                    this.titleLabel.text().set(TextObject.translation(this.client.integratedServer == null ? "quantum.screen.worldLoad.loading" : "quantum.screen.worldLoad.loadingFromServer"));
                    this.subTitleLabel.text().set(TextObject.translation("quantum.screen.worldLoad.preparingChunks"));
                    return;
                }
                if (world.getChunksLoaded() == chunksToLoad) {
                    this.done = true;

                    this.client.worldRenderer = new WorldRenderer(worldAccess);

                    this.client.renderWorld = true;
                    this.client.showScreen(null);
                    return;
                }

                float ratio = (float) world.getChunksLoaded() / chunksToLoad;
                String percent = (int) (100 * ratio) + "%";
                ratio = Mth.clamp(ratio, 0, 1);
                this.subTitleLabel.text().set(TextObject.translation("quantum.screen.worldLoad.chunksLoading", world.getChunksLoaded(), chunksToLoad, percent));

                if (this.nextLog <= System.currentTimeMillis()) {
                    this.nextLog = System.currentTimeMillis() + 2000;
                    QuantumClient.LOGGER.info("Loading world: {}", percent);
                }

                // Draw progressbar
                int x = this.size.width / 2 - 100;
                int y = this.size.height / 3 + 50;

                int width = 200;
                int height = 5;

                renderer.fill(x, y, width, height, PROGRESS_BG);
                renderer.fill(x, y, (int) (width * ratio), height, PROGRESS_FG);
            } else if (subTitleLabel != null) {
                this.subTitleLabel.text().setRaw("");
            }
        } else if (subTitleLabel != null) {
            this.subTitleLabel.text().setRaw("");
        }
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    public void onLogin() {
        try {
            this.loggedIn = true;
            this.done = true;

            this.client.renderWorld = true;
            ClientWorld clientWorld = this.client.world;
            if (clientWorld instanceof ClientWorld) {
                this.client.worldRenderer = new WorldRenderer(clientWorld);
            } else {
                throw new IllegalStateException("Unexpected world type: " + null);
            }
            this.client.showScreen(null);
        } catch (Exception e) {
            QuantumClient.LOGGER.error("Failed to handle login:", e);
            this.client.exitWorldAndThen(() -> {
                this.client.showScreen(new DisconnectedScreen("Internal error when handling login", false));
            });
        }
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    public Label getDescriptionLabel() {
        return descriptionLabel;
    }

    public Label getSubTitleLabel() {
        return subTitleLabel;
    }

    public DeathScreen getCloseScreen() {
        return closeScreen;
    }

    public void setCloseScreen(DeathScreen closeScreen) {
        this.closeScreen = closeScreen;
    }

    public int getChunksToLoadCount() {
        return chunksToLoadCount;
    }

    public void setChunksToLoadCount(int chunksToLoadCount) {
        this.chunksToLoadCount = chunksToLoadCount;
    }

    public WorldStorage getStorage() {
        return storage;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
