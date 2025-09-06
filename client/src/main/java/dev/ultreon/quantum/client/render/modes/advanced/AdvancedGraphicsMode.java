package dev.ultreon.quantum.client.render.modes.advanced;

import dev.ultreon.quantum.client.render.modes.GraphicsMode;

public class AdvancedGraphicsMode extends GraphicsMode {
    private boolean enabled = false;

    @Override
    public void enable() {
        if (enabled) return;
        enabled = true;

        var worldPass = new WorldRenderPass();
        worldPass.create();
        var reflectionPass = new ReflectionRenderPass(worldPass);
        reflectionPass.create();
        var outputPass = new OutputRenderPass(worldPass, reflectionPass);
        outputPass.create();

        addRenderPass(worldPass);
        addRenderPass(reflectionPass);
        addRenderPass(outputPass);
    }

    @Override
    public void disable() {
        if (!enabled) return;

        super.disable();

        enabled = false;
    }
}
