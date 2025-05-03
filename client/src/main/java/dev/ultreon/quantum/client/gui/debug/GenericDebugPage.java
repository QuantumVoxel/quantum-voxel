package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.audio.music.MusicManager;
import dev.ultreon.quantum.client.input.GameInput;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.EntityHit;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.ChunkBuildInfo;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class GenericDebugPage implements DebugPage {
    @Override
    public void render(DebugPageContext context) {
        var client = context.client();
        var world = client.world;
        var worldRenderer = client.worldRenderer;
        if (worldRenderer != null && worldRenderer.isDisposed()) worldRenderer = null;
        if (world != null && world.isDisposed()) world = null;

        context.left("GDX Status")
                .left("Mesh Status", Mesh.getManagedStatus())
                .left();

        // World
        @Nullable IntegratedServer integratedServer = client.integratedServer;
        if (integratedServer != null) {
            context.left("Integrated Server")
                    .left("Server TPS", integratedServer.getCurrentTps())
                    .left("Packets", "rx = " + IConnection.rx.get() + ", tx = " + IConnection.tx.get())
                    .left();
        } else {
            context.left("Server Connection")
                    .left("Server TPS", "N/A (Coming Soon!)")
                    .left("Packets", "rx = " + IConnection.rx.get() + ", tx = " + IConnection.tx.get())
                    .left();
        }

        context.left("Music");
        context.left("Music Volume", MusicManager.get().getVolume());
        context.left("Time Until Next Track", String.format(Locale.getDefault(), "%.2f seconds", MusicManager.get().getTimeUntilNextTrack()));

        if (world != null) {
            // Player
            Player player = client.player;
            if (player != null) {
                context.left("Player");
                BlockVec blockVec = player.getBlockVec();
                Vec3i sectionPos = context.block2sectionPos(blockVec);
                @Nullable ClientChunkAccess chunk = world.getChunkAt(blockVec);
                BlockVec localBlockVec = blockVec.chunkLocal();

                RegistryKey<Biome> biome = world.getBiome(blockVec);
                context.left("XYZ", player.getPosition())
                        .left("Block XYZ", blockVec)
                        .left("Chunk XYZ", sectionPos);
                if (chunk != null) {
                    int sunlight = chunk.getSunlight(localBlockVec.vec());
                    int blockLight = chunk.getBlockLight(localBlockVec.vec());

                    context.left("Biome", biome == null ? null : biome.id())
                            .left("Chunk Offset", chunk.getRenderOffset())
                            .left("Sunlight", sunlight)
                            .left("Block Light", blockLight);
                }
                context.left("Chunk Shown", world.getChunkAt(blockVec) != null);
                Hit hit = client.hit;
                if (hit != null)
                    context.left("Break Progress", world.getBreakProgress(new BlockVec(hit.getBlockVec())));
                context.left();
            }

            context.left("World");
            if (worldRenderer != null) {
                context.left("Visible Chunks", worldRenderer.getVisibleChunks() + "/" + worldRenderer.getLoadedChunksCount());
            }

            context.left("Chunk Mesh Disposes", WorldRenderer.getChunkMeshFrees());
            if (client.isSinglePlayer()) {
                context.left("Chunk Loads", ValueTracker.getChunkLoads())
                        .left("Chunk Unloads", ServerWorld.getChunkUnloads());
            }

            context.left("Chunk Renderable", ValueTracker.getRenderableCount())
                    .left("Avg. Chunk Renderables", ValueTracker.getAverageRenderables())
                    .left();

            ValueTracker.resetRenderables();
        }

        // Queues
        context.left();
        context.left("Executor Sizes");
        context.left("Client Queue", QuantumClient.get().getQueueSize());
        if (integratedServer != null) {
            context.left("Server Queue", integratedServer.getQueueSize());
        }

        // Chunk
        LocalPlayer localPlayer = client.player;
        if (world != null && localPlayer != null) {
            ClientChunkAccess chunkAccess = world.getChunk(localPlayer.getChunkVec());
            if (chunkAccess instanceof ClientChunk) {
                ClientChunk chunk = (ClientChunk) chunkAccess;
                context.right();
                context.right("Chunk");
                context.right("Pos", chunk.getVec());

                context.right();
                context.right("Chunk Info");
                context.right("Client Load Time", chunk.info.loadDuration);
                ChunkBuildInfo build = chunk.info.build;
                if (build != null) context.right("Server Build Time", build.buildDuration);
            }
        }

        // Cursor
        @NotNull Hit cursor = client.cursor;
        if (cursor instanceof BlockHit && cursor.isCollide()) {
            BlockHit blockHit = (BlockHit) cursor;
            BlockState block = blockHit.getBlockMeta();
            if (block != null && !block.isAir()) {
                context.right();
                context.right("Cursor");
                context.right("Block", block.getBlock().getId());
                context.right("Pos", cursor.getBlockVec());
                context.right("Next", blockHit.getNext());
            }
        } else if (cursor instanceof EntityHit) {
            EntityHit entityHit = (EntityHit) cursor;
            Entity entity = entityHit.getEntity();
            if (entity != null) {
                context.right();
                context.right("Cursor");
                context.right("Entity ID", entity.getId());
                context.right("Entity Key", entity.getType().getId());
                context.right("Pos", cursor.getBlockVec());
            }
        }

        context.right();
        context.right("GUI");
        context.right("Width", Gdx.graphics.getWidth());
        context.right("Height", Gdx.graphics.getHeight());
        context.right("Scale", Gdx.graphics.getDensity());
        context.right("Framerate", Gdx.graphics.getFramesPerSecond());
        context.right("Delta", Gdx.graphics.getDeltaTime());
        context.right("Screen Name", client.screen == null ? "N/A" : client.screen.getClient().getName());

        context.right();
        context.right("Memory");
        context.right("Native", Gdx.app.getNativeHeap());
        context.right("Java", Gdx.app.getJavaHeap());

        if (!GamePlatform.get().isWeb()) {
            context.right();
            context.right("Java Version", System.getProperty("java.version"));
            context.right("Java VM", System.getProperty("java.vm.name"));
            context.right("Java VM Version", System.getProperty("java.vm.version"));
            context.right("Java VM Vendor", System.getProperty("java.vm.vendor"));
            context.right("Java VM Spec", System.getProperty("java.vm.specification.version"));

            context.right();
            context.right("OS");
            context.right("Name", System.getProperty("os.name"));
            context.right("Version", System.getProperty("os.version"));
        } else {
            context.right();
            context.right("Web Browser");
            context.right("User Agent", GamePlatform.get().getUserAgent());
            context.right("Language", GamePlatform.get().getLanguage());
        }

        GameInput current = GameInput.getCurrent();
        context.left("Current Input", current == null ? "N/A" : current.getName());
    }
}
