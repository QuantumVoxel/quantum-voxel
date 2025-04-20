package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.graphics.Mesh;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.IntegratedServer;
import dev.ultreon.quantum.client.QuantumClient;
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

public class ValueTrackerPage implements DebugPage {
    @Override
    public void render(DebugPageContext context) {
        context.left("Average Renderables", ValueTracker.getAverageRenderables());
        context.left("Chunk Loads", ValueTracker.getChunkLoads());
        context.left("Vertex Count", ValueTracker.getVertexCount());
        context.left("Renderable Count", ValueTracker.getRenderableCount());
        context.left("Shader Switches", ValueTracker.getShaderSwitches());
        context.left("Texture Bindings", ValueTracker.getTextureBindings());
    }
}
