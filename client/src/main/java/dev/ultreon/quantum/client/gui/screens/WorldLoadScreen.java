package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;
import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.*;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.network.packets.c2s.C2SRequestChunkLoadPacket;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.vec.ChunkVec;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public class WorldLoadScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldLoadScreen.class);
    public static final @NotNull Color PROGRESS_BG = new Color(0xffffff80);
    public static final @NotNull Color PROGRESS_FG = new Color(0xff0040ff);
    @Getter
    private Label titleLabel;
    @Getter
    private Label descriptionLabel;
    private Label subTitleLabel;
    private final WorldStorage storage;
    private long nextLog;
    private ServerWorld world;
    @Setter
    private DeathScreen closeScreen;
    private boolean done = false;
    private volatile boolean loggedIn;
    private int chunksToLoadCount;

    public WorldLoadScreen(WorldStorage storage) {
        super(TextObject.translation("quantum.screen.world_load"));
        this.storage = storage;
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        IntegratedServer server = new IntegratedServer(this.storage);
        this.client.integratedServer = server;

        this.client.add("Integrated Server", server);

        server.init();

        this.world = server.getDimManager().getWorld(DimensionInfo.OVERWORLD);
        this.client.openedWorld = this.storage;

        this.titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 - 25))
                .scale(2));

        this.descriptionLabel = builder.add(Label.of("Preparing")
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 + 3)));

        this.subTitleLabel = builder.add(Label.of()
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 + 31)));

        Thread worldLoading = new Thread(this::run, "World Loading");
        worldLoading.start();
    }

    @Override
    public boolean onClose(@Nullable Screen next) {
        if (!this.client.renderWorld) return false;

        DeathScreen closeScreen = this.closeScreen;
        if (next == null && closeScreen != null) {
            this.client.showScreen(closeScreen);
            return false;
        }
        return super.onClose(next);
    }

    @SuppressWarnings("BusyWait")
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

            while (!loggedIn) {
                Thread.sleep(100);
            }

            ChunkVec chunkVec = Objects.requireNonNull(this.client.player, "Player is null").getChunkVec();
            this.client.connection.send(new C2SRequestChunkLoadPacket(chunkVec));

            this.message("Waiting for server to finalize...");
        } catch (InterruptedException e) {
            QuantumClient.LOGGER.info("World load interrupted");
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
    public boolean canClose() {
        return this.done;
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    public void onLogin() {
        this.loggedIn = true;
        this.done = true;

        this.client.renderWorld = true;
        ClientWorldAccess world1 = this.client.world;
        if (world1 instanceof ClientWorld) {
            ClientWorld clientWorld = (ClientWorld) world1;
            this.client.worldRenderer = new WorldRenderer(clientWorld);
        } else {
            throw new IllegalStateException("Unexpected world type: " + null);
        }
        this.client.showScreen(null);
    }
}
