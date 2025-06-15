package dev.ultreon.quantum.client.render;

import org.jetbrains.annotations.NotNull;

public class PBRRenderBufferSource extends RenderBufferSource {

    public final Mode mode;

    public PBRRenderBufferSource(Mode mode) {
        this.mode = mode;
    }

    public enum Mode {
        NORMAL,
        DEPTH
    }

    @Override
    public @NotNull RenderBuffer createBuffer(RenderPass pass) {
        return new PBRRenderBuffer(pass, forceEnvironment);
    }
}
