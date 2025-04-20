package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.util.RenderObject;
import dev.ultreon.quantum.util.InvalidThreadException;
import org.jetbrains.annotations.Nullable;

public class Skybox extends RenderObject implements RenderableProvider, Disposable {
    public static final Color NULL_COLOR = new Color(0, 0, 0, 0);
    private final static int riseSetDuration = ClientWorld.DAY_CYCLE / 24;

    public Color topColor = new Color();
    public Color midColor = new Color();
    public Color bottomColor = new Color();

    public Color posZColor = new Color();
    public Color negZColor = new Color();
    @Nullable public Model model;
    @Nullable public ModelInstance modelInstance;
    public static boolean debug = false;

    public Skybox() {
        renderPass = RenderPass.SKYBOX;
    }

    public void update(long daytime) {
        timeMix(daytime, ClientWorld.DAY_TOP_COLOR, ClientWorld.NIGHT_TOP_COLOR, topColor);
        timeMix(daytime, ClientWorld.DAY_BOTTOM_COLOR, ClientWorld.NIGHT_BOTTOM_COLOR, midColor);
        timeMix(daytime, ClientWorld.DAY_BOTTOM_COLOR, ClientWorld.NIGHT_BOTTOM_COLOR, bottomColor);

        sunRiseSetMix(daytime, ClientWorld.SUN_RISE_COLOR, null, posZColor);
        sunRiseSetMix(daytime, null, ClientWorld.SUN_RISE_COLOR, negZColor);
    }

    private static void timeMix(long daytime, Color dayColor, Color nightColor, Color output) {
        if (daytime < Skybox.riseSetDuration / 2) {
            ClientWorld.mixColors(
                    dayColor, nightColor, output,
                    0.5f + daytime / (float) Skybox.riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - Skybox.riseSetDuration / 2) {
            output.set(dayColor);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + Skybox.riseSetDuration / 2) {
            ClientWorld.mixColors(
                    nightColor, dayColor, output,
                    (daytime - ((double) ClientWorld.DAY_CYCLE / 2 - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE - Skybox.riseSetDuration / 2) {
            output.set(nightColor);
        } else {
            ClientWorld.mixColors(
                    dayColor, nightColor, output,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
        }
    }

    private static void sunRiseSetMix(long daytime, @Nullable Color sunRiseColor, @Nullable Color sunSetColor, Color output) {
        if (sunRiseColor == null)
            sunRiseColor = NULL_COLOR;

        if (sunSetColor == null)
            sunSetColor = NULL_COLOR;

        if (daytime < Skybox.riseSetDuration / 2) {
            ClientWorld.mixColors(NULL_COLOR, sunRiseColor, output, 0.5f + daytime / (float) Skybox.riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - Skybox.riseSetDuration / 2) {
            output.set(NULL_COLOR);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2) {
            ClientWorld.mixColors(sunSetColor, NULL_COLOR, output, (daytime - ((double) ClientWorld.DAY_CYCLE / 2)) / Skybox.riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + Skybox.riseSetDuration / 2) {
            ClientWorld.mixColors(NULL_COLOR, sunSetColor, output, (daytime - ((double) ClientWorld.DAY_CYCLE / 2 - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE - Skybox.riseSetDuration / 2) {
            output.set(NULL_COLOR);
        } else {
            ClientWorld.mixColors(sunRiseColor, NULL_COLOR, output, (daytime - (ClientWorld.DAY_CYCLE - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (modelInstance == null || model == null) return;
        modelInstance.getRenderables(renderables, pool);

        for (int i = 0; i < renderables.size; i++) {
            Renderable renderable = renderables.get(i);
            renderable.userData = Shaders.SKYBOX.get();
        }
    }

    @Override
    public void dispose() {
        if (!QuantumClient.isOnRenderThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);

        Model model = this.model;
        this.model = null;
        this.modelInstance = null;
        if (model != null)
            model.dispose();
    }

    public void render0(RenderBufferSource bufferSource) {
        super.render(bufferSource);
    }

    @Override
    public void render(RenderBufferSource bufferSource) {

    }
}
