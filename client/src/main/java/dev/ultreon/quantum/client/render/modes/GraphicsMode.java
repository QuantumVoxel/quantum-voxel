package dev.ultreon.quantum.client.render.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import de.damios.guacamole.Preconditions;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.context.RenderContext;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.client.util.Toggleable;
import dev.ultreon.quantum.featureflags.Feature;
import dev.ultreon.quantum.featureflags.FeatureFlag;
import dev.ultreon.quantum.featureflags.FeatureSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraphicsMode implements Toggleable, Feature {
    private final Array<RenderPass> renderPasses = new Array<>();
    private final SpriteBatch spriteBatch = GamePlatform.get().createSpriteBatch();
    private int width;
    private int height;

    private @Nullable FeatureFlag featureFlag = null;

    public GraphicsMode() {

    }

    public GraphicsMode(@NotNull FeatureFlag featureFlag) {
        Preconditions.checkNotNull(featureFlag, "featureFlag cannot be null");
        this.featureFlag = featureFlag;
    }

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

        this.spriteBatch.setProjectionMatrix(this.spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height));
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
        RenderPass outPass = null;
        for (RenderPass renderPass : renderPasses) {
            renderPass.render(bufferSource, context);
            outPass = renderPass;
        }

        if (outPass == null) throw new IllegalStateException("No output pass found!");
        this.spriteBatch.begin();
        this.spriteBatch.setShader(null);
        this.spriteBatch.draw(outPass.getTextures()[0], 0, height, width, -height);
        this.spriteBatch.end();
    }

    @Override
    public void disable() {
        for (var pass : renderPasses) {
            pass.dispose();
        }
        this.renderPasses.clear();
    }

    public abstract String getTranslationId();

    @Override
    public boolean isEnabled(FeatureSet featureSet) {
        return featureFlag == null || featureSet.isEnabled(featureFlag);
    }
}
