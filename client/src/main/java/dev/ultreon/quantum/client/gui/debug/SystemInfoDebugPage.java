package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.graphics.Mesh;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.util.RenderableArray;
import dev.ultreon.quantum.client.world.ChunkMesh;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.BlockHitResult;
import dev.ultreon.quantum.util.HitResult;
import dev.ultreon.quantum.world.BlockPos;
import dev.ultreon.quantum.world.ChunkPos;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Nullable;

public class SystemInfoDebugPage implements DebugPage {
    @Override
    public void render(DebugPageContext context) {
        var client = context.client();
        var world = client.world;
        var worldRenderer = client.worldRenderer;
        if (worldRenderer != null && worldRenderer.isDisposed()) worldRenderer = null;
        if (world != null && world.isDisposed()) world = null;

        context.left("CPU")
                .left("", Mesh.getManagedStatus())
                .left();

        // World
        @Nullable IntegratedServer integratedServer = client.integratedServer;
        if (integratedServer != null) {
            context.left("Integrated Server")
                    .left("Server TPS", integratedServer.getCurrentTps())
//                    .left("Packets", "rx = " + MemoryConnection.getPacketsReceived() + ", tx = " + MemoryConnection.getPacketsSent())
                    .left();
        }

        context.left("Meshes")
                .left("Meshes Disposed", ChunkMesh.getMeshesDisposed())
                .left("Vertex Count", WorldRenderer.getVertexCount())
                .left();

        context.left("Renderables")
                .left("Global Size", RenderableArray.getGlobalSize())
                .left("Obtained Renderables", ValueTracker.getObtainedRenderables())
                .left("Obtain Requests", ValueTracker.getObtainRequests())
                .left("Free Requests", ValueTracker.getFreeRequests())
                .left("Flush Requests", ValueTracker.getFlushRequests())
                .left();

        if (world != null) {
            // Player
            Player player = client.player;
            if (player != null) {
                context.left("Player");
                BlockPos blockPosition = player.getBlockPos();
                Vec3i sectionPos = context.block2sectionPos(blockPosition);
                ChunkPos chunkPos = player.getChunkPos();
                ClientChunk chunk = world.getChunk(chunkPos);
                BlockPos localBlockPos = World.toLocalBlockPos(blockPosition);

                context.left("XYZ", player.getPosition())
                        .left("Block XYZ", blockPosition)
                        .left("Chunk XYZ", sectionPos)
                        .left("Biome", Registries.BIOME.getId(world.getBiome(blockPosition)));
                if (chunk != null) {
                    int sunlight = chunk.getSunlight(localBlockPos.vec());
                    int blockLight = chunk.getBlockLight(localBlockPos.vec());

                    context.left("Chunk Offset", chunk.renderOffset)
                            .left("Sunlight", sunlight)
                            .left("Block Light", blockLight);
                }
                context.left("Chunk Shown", world.getChunk(chunkPos) != null);
                HitResult hitResult = client.hitResult;
                if (hitResult != null)
                    context.left("Break Progress", world.getBreakProgress(new BlockPos(hitResult.getPos())));
                context.left();
            }

            context.left("World");
            if (worldRenderer != null) {
                context.left("Visible Chunks", worldRenderer.getVisibleChunks() + "/" + worldRenderer.getLoadedChunks());
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

        BlockHitResult cursor = client.cursor;
        if (cursor.isCollide()) {
            BlockProperties block = cursor.getBlockMeta();
            if (block != null && !block.isAir()) {
                context.right("Block", block);
            }
        }
    }
}
