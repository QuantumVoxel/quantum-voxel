package dev.ultreon.quantum.client.gui.debug;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.jetbrains.annotations.Nullable;

public class ChunkInfoDebugPage implements DebugPage {
    @Override
    public void render(DebugPageContext context) {
        context.left("Chunk Info");

        QuantumClient client = context.client();

        @Nullable ClientWorldAccess world = client.world;
        LocalPlayer player = client.player;
        if (world == null) return;
        if (player == null) return;

        ChunkVec chunkVec = player.getChunkVec();
        @Nullable ClientChunkAccess chunk = world.getChunk(chunkVec);

        if (chunk == null) {
            context.left("Chunk", "N/A");
            return;
        }

        context.left("Chunk", chunkVec.toString());
        context.left("X", chunkVec.getIntX());
        context.left("Z", chunkVec.getIntZ());

        context.left("Heightmap", chunk.getHeight(chunkVec.getIntX(), chunkVec.getIntZ()));
        context.left("Render Offset", chunk.getRenderOffset());
    }
}
