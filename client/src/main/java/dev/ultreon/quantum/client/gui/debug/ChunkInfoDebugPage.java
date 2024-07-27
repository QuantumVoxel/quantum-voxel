package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.debug.profiler.ProfileData;
import dev.ultreon.quantum.debug.profiler.Section;
import dev.ultreon.quantum.debug.profiler.ThreadSection;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkInfoDebugPage implements DebugPage {
    @Override
    public void render(DebugPageContext context) {
        context.left("Chunk Info");

        QuantumClient client = context.client();

        @Nullable ClientWorldAccess world = client.world;
        LocalPlayer player = client.player;
        if (world == null) return;
        if (player == null) return;

        ChunkPos chunkPos = player.getChunkPos();
        @Nullable ClientChunkAccess chunk = world.getChunk(chunkPos);

        if (chunk == null) {
            context.left("Chunk", "N/A");
            return;
        }

        context.left("Chunk", chunkPos.toString());
        context.left("X", chunkPos.x());
        context.left("Z", chunkPos.z());

        context.left("Heightmap", chunk.getHighest(chunkPos.x(), chunkPos.z()));
        context.left("Render Offset", chunk.getRenderOffset());

        Model model = chunk.getModel();
        if (model != null) {
            context.left();
            context.left("Chunk Model");
            context.left("Nodes", model.nodes.size);
            context.left("Meshes", model.meshes.size);
            context.left("Materials", model.materials.size);
            context.left("Mesh Parts", model.meshParts.size);
            context.left("Animations", model.animations.size);
        }

        ModelInstance instance = chunk.getModelInstance();
        if (instance != null) {
            context.left();
            context.left("Chunk Model Instance");
            context.left("Nodes", instance.nodes.size);
            context.left("Materials", instance.materials.size);
            context.left("Animations", instance.animations.size);
        }
    }
}
