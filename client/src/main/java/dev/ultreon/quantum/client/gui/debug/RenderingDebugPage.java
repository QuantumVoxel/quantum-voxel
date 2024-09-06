package dev.ultreon.quantum.client.gui.debug;

import dev.ultreon.quantum.client.Main;

public class RenderingDebugPage implements DebugPage {

    private int drawCalls;
    private int calls;
    private int shaderSwitches;
    private int textureBindings;
    private float maxVertexCount;
    private float averageVertexCount;

    @Override
    public void render(DebugPageContext context) {
        context.left("GL Draw Calls = {}", Main.getGlProfiler().getDrawCalls() - drawCalls)
                .left("GL Calls = {}", Main.getGlProfiler().getCalls() - calls)
                .left("Shader Switches = {}", Main.getGlProfiler().getShaderSwitches() - shaderSwitches)
                .left("Texture Bindings = {}", Main.getGlProfiler().getTextureBindings() - textureBindings)
                .left("Max Vertex Count = {}", maxVertexCount)
                .left("Average Vertex Count = {}", averageVertexCount)
                .left("Vertex Count = {}", Main.getGlProfiler().getVertexCount().latest);
        drawCalls = Main.getGlProfiler().getDrawCalls();
        calls = Main.getGlProfiler().getCalls();
        shaderSwitches = Main.getGlProfiler().getShaderSwitches();
        textureBindings = Main.getGlProfiler().getTextureBindings();
        maxVertexCount = Main.getGlProfiler().getVertexCount().max;
        averageVertexCount = Main.getGlProfiler().getVertexCount().average;
    }
}
