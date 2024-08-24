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
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.shader.Shaders;
import dev.ultreon.quantum.util.InvalidThreadException;

public class Skybox implements RenderableProvider, Disposable {
    public static final Color NULL_COLOR = new Color(0, 0, 0, 0);
    private final static int riseSetDuration = ClientWorld.DAY_CYCLE / 24;

    public Color topColor = new Color();
    public Color midColor = new Color();
    public Color bottomColor = new Color();

    public Color posZColor = new Color();
    public Color negZColor = new Color();
    public Model model;
    public ModelInstance modelInstance;
    private final Color tmp = new Color();
    public static boolean debug = false;

    public void update(int daytime, float deltaTime) {
        timeMix(daytime, ClientWorld.DAY_TOP_COLOR, ClientWorld.NIGHT_TOP_COLOR, topColor);
        timeMix(daytime, ClientWorld.DAY_BOTTOM_COLOR, ClientWorld.NIGHT_BOTTOM_COLOR, midColor);
        if (QuantumClient.get().player != null) {
            timeMix(daytime, ClientWorld.DAY_BOTTOM_COLOR, ClientWorld.NIGHT_BOTTOM_COLOR, bottomColor);
        } else {
            timeMix(daytime, ClientWorld.DAY_BOTTOM_COLOR, ClientWorld.NIGHT_BOTTOM_COLOR, bottomColor);
        }

        sunRiseSetMix(daytime, ClientWorld.SUN_RISE_COLOR, null, posZColor);
        sunRiseSetMix(daytime, null, ClientWorld.SUN_RISE_COLOR, negZColor);
    }

    private Color posYMix(LocalPlayer player, float deltaTime, Color color, Color voidColor, Color output) {
        int start = ClientWorld.VOID_Y_START;
        int end = ClientWorld.VOID_Y_END;

        if (player.getPosition(deltaTime).y < start) {
            return voidColor;
        } else if (player.getPosition(deltaTime).y >= end) {
            return ClientWorld.mixColors(
                    voidColor, color, output,
                    (player.getPosition(deltaTime).y - start) / (float) (end - start));
        } else {
            return color;
        }
    }

    private static Color timeMix(int daytime, Color dayColor, Color nightColor, Color output) {
        if (daytime < Skybox.riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    dayColor, nightColor, output,
                    0.5f + daytime / (float) Skybox.riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - Skybox.riseSetDuration / 2) {
            return output.set(dayColor);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + Skybox.riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    nightColor, dayColor, output,
                    (daytime - ((double) ClientWorld.DAY_CYCLE / 2 - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE - Skybox.riseSetDuration / 2) {
            return output.set(nightColor);
        } else {
            return ClientWorld.mixColors(
                    dayColor, nightColor, output,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
        }
    }

    private static Color sunRiseSetMix(int daytime, Color sunRiseColor, Color sunSetColor, Color output) {
        if (sunRiseColor == null)
            sunRiseColor = NULL_COLOR;

        if (sunSetColor == null)
            sunSetColor = NULL_COLOR;

        if (daytime < Skybox.riseSetDuration / 2)
            return ClientWorld.mixColors(NULL_COLOR, sunRiseColor, output, 0.5f + daytime / (float) Skybox.riseSetDuration);
        else if (daytime <= ClientWorld.DAY_CYCLE / 2 - Skybox.riseSetDuration / 2)
            return output.set(NULL_COLOR);
        else if (daytime <= ClientWorld.DAY_CYCLE / 2)
            return ClientWorld.mixColors(sunSetColor, NULL_COLOR, output, (daytime - ((double) ClientWorld.DAY_CYCLE / 2)) / Skybox.riseSetDuration);
        else if (daytime <= ClientWorld.DAY_CYCLE / 2 + Skybox.riseSetDuration / 2)
            return ClientWorld.mixColors(NULL_COLOR, sunSetColor, output, (daytime - ((double) ClientWorld.DAY_CYCLE / 2 - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
        else if (daytime <= ClientWorld.DAY_CYCLE - Skybox.riseSetDuration / 2)
            return output.set(NULL_COLOR);
        else
            return ClientWorld.mixColors(sunRiseColor, NULL_COLOR, output, (daytime - (ClientWorld.DAY_CYCLE - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration);
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
}
