package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.graphics.Mesh;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.util.RenderableArray;
import dev.ultreon.quantum.client.world.ChunkMesh;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.debug.ValueTracker;

public class RenderingDebugPage implements DebugPage {
    @Override
    public void render(DebugPageContext context) {

        QuantumClient client = QuantumClient.get();
        context.left("Rendering")
                .left("Mesh Status", Mesh.getManagedStatus())
                .left("Texture Status", client.getTextureManager().getManagedStatus())
                .left();

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

        context.left("Cubemaps")
                .left("Cubemaps Loaded", client.getCubemapManager().getLoadedCount())
                .left();

        context.right("Background Layer")
                .right("Active", RenderLayer.BACKGROUND.getActiveCount())
                .right("Inactive", RenderLayer.BACKGROUND.getInactiveCount())
                .right();

        context.right("World Layer")
                .right("Active", RenderLayer.WORLD.getActiveCount())
                .right("Inactive", RenderLayer.WORLD.getInactiveCount())
                .right();

        context.left("World Render Pool")
                .left("Pool Free", WorldRenderer.getPoolFree())
                .left("Pool Max", WorldRenderer.getPoolMax())
                .left("Pool Peak", WorldRenderer.getPoolPeak())
                .left();
    }
}
