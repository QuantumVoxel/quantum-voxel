package dev.ultreon.quantum.client.render.modes.basic;

import de.damios.guacamole.Preconditions;
import dev.ultreon.quantum.client.render.modes.GraphicsMode;
import dev.ultreon.quantum.featureflags.Feature;
import dev.ultreon.quantum.featureflags.FeatureFlag;
import dev.ultreon.quantum.featureflags.FeatureSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicGraphicsMode extends GraphicsMode {
    private boolean enabled;

    @Override
    public void enable() {
        if (enabled) return;
        enabled = true;

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

    @Override
    public String getTranslationId() {
        return "quantum.graphics.mode.basic";
    }
}
