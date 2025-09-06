package dev.ultreon.quantum.client.render.modes.basic;

import dev.ultreon.quantum.client.render.modes.GraphicsMode;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import org.jetbrains.annotations.NotNull;

public class BasicGraphicsMode extends GraphicsMode {
    private boolean enabled;

    @Override
    public void enable() {
        var basicRenderPass = new BasicRenderPass();
        basicRenderPass.create();

        addRenderPass(basicRenderPass);
    }

    @Override
    public void disable() {
        if (!enabled) return;

        super.disable();

        enabled = false;
    }
}
