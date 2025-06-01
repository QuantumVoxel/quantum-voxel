package dev.ultreon.quantum.client.gui.debug;

import dev.ultreon.quantum.debug.ValueTracker;

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
