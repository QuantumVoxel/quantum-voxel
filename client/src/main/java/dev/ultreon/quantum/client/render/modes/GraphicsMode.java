package dev.ultreon.quantum.client.render.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.context.RenderContext;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.client.util.Toggleable;
import org.jetbrains.annotations.NotNull;

public abstract class GraphicsMode implements Toggleable {
    private final Array<RenderPass> renderPasses = new Array<>();
    private final SpriteBatch spriteBatch = GamePlatform.get().createSpriteBatch();
    private int width;
    private int height;

    protected void addRenderPass(RenderPass renderPass) {
        renderPasses.add(renderPass);
    }

    protected final @NotNull RenderPass getOutputPass() {
        return renderPasses.get(renderPasses.size - 1);
    }

    public final void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        for (var pass : renderPasses) {
            pass.setSize(width, height);
        }

        resize(width, height);
    }

    protected void resize(int width, int height) {

    }

    public final void verify() {
        for (RenderPass renderPass : renderPasses) {
            renderPass.verify();
        }

        int length = getOutputPass().getTextures().length;
        if (length != 1) {
            throw new IllegalStateException("Output pass must have exactly one texture, but has " + length + "!");
        }
    }

    public void render(RenderBufferSource bufferSource, RenderContext context) {
        for (RenderPass renderPass : renderPasses) {
            renderPass.render(bufferSource, context);
        }

        RenderPass outPass = getOutputPass();
        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
        this.spriteBatch.setShader(null);
        outPass.getTextures()[0].bind(0);
        this.spriteBatch.begin();
        this.spriteBatch.draw(outPass.getTextures()[0], 0, height, width, -height);
        this.spriteBatch.end();
    }

    @Override
    public void disable() {
        for (var pass : renderPasses) {
            pass.dispose();
        }
    }
}
