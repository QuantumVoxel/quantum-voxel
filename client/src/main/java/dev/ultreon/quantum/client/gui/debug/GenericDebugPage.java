package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.graphics.Mesh;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

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

        if (world != null) {
            // Player
            Player player = client.player;
            if (player != null) {
                context.left("Player");
                BlockVec blockVec = player.getBlockVec();
                Vec3i sectionPos = context.block2sectionPos(blockVec);
                @Nullable ClientChunkAccess chunk = world.getChunkAt(blockVec);
                BlockVec localBlockVec = blockVec.chunkLocal();

                context.left("XYZ", player.getPosition())
                        .left("Block XYZ", blockVec)
                        .left("Chunk XYZ", sectionPos)
                        .left("Biome", Registries.BIOME.getId(world.getBiome(blockVec)));
                if (chunk != null) {
                    int sunlight = chunk.getSunlight(localBlockVec.vec());
                    int blockLight = chunk.getBlockLight(localBlockVec.vec());

                    context.left("Chunk Offset", chunk.getRenderOffset())
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

            context.left("Pool Free", WorldRenderer.getPoolFree())
                    .left("Pool Max", WorldRenderer.getPoolMax())
                    .left("Pool Peak", WorldRenderer.getPoolPeak())
                    .left();
        }

        BlockHit cursor = client.cursor;
        if (cursor != null && cursor.isCollide()) {
            BlockState block = cursor.getBlockMeta();
            if (block != null && !block.isAir()) {
                context.right("Block", block);
            }
        }
    }
}
