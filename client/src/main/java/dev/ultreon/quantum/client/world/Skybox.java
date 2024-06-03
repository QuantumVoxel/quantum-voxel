package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.util.RgbColor;

import static dev.ultreon.quantum.util.RgbColor.rgba;

public class Skybox {
    private final static int riseSetDuration = ClientWorld.DAY_CYCLE / 24;

    public Color topColor = new Color();
    public Color midColor = new Color();
    public Color bottomColor = new Color();

    public Color posZColor = new Color();
    public Color negZColor = new Color();

    public void update(int daytime, float deltaTime) {
        topColor.set(timeMix(daytime, ClientWorld.DAY_TOP_COLOR, ClientWorld.NIGHT_TOP_COLOR));
        midColor.set(timeMix(daytime, ClientWorld.DAY_BOTTOM_COLOR, ClientWorld.NIGHT_BOTTOM_COLOR));
        bottomColor.set(posYMix(QuantumClient.get().player, deltaTime, timeMix(daytime, ClientWorld.DAY_BOTTOM_COLOR, ClientWorld.NIGHT_BOTTOM_COLOR), ClientWorld.VOID_COLOR));

        posZColor.set(sunRiseSetMix(daytime, ClientWorld.SUN_RISE_COLOR, null));
        negZColor.set(sunRiseSetMix(daytime, null, ClientWorld.SUN_RISE_COLOR));
    }

    private Color posYMix(LocalPlayer player, float deltaTime, Color color, RgbColor voidColor) {
        int start = ClientWorld.VOID_Y_START;
        int end = ClientWorld.VOID_Y_END;

        if (player.getPosition(deltaTime).y < start) {
            return voidColor.toGdx();
        } else if (player.getPosition(deltaTime).y >= end) {
            return ClientWorld.mixColors(
                    voidColor, RgbColor.gdx(color),
                    (player.getPosition(deltaTime).y - start) / (float) (end - start)).toGdx();
        } else {
            return color;
        }
    }

    private static Color timeMix(int daytime, RgbColor dayTopColor, RgbColor nightTopColor) {
        if (daytime < Skybox.riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    dayTopColor, nightTopColor,
                    0.5f + daytime / (float) Skybox.riseSetDuration).toGdx();
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - Skybox.riseSetDuration / 2) {
            return dayTopColor.toGdx();
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + Skybox.riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    nightTopColor, dayTopColor,
                    (daytime - ((double) ClientWorld.DAY_CYCLE / 2 - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration).toGdx();
        } else if (daytime <= ClientWorld.DAY_CYCLE - Skybox.riseSetDuration / 2) {
            return nightTopColor.toGdx();
        } else {
            return ClientWorld.mixColors(
                    dayTopColor, nightTopColor,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration).toGdx();
        }
    }

    private static Color sunRiseSetMix(int daytime, RgbColor sunRiseColor, RgbColor sunSetColor) {
        final var nullColor = rgba(0x00000000);

        if (sunRiseColor == null) {
            sunRiseColor = rgba(0x00000000);
        }

        if (sunSetColor == null) {
            sunSetColor = rgba(0x00000000);
        }

        if (daytime < Skybox.riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    nullColor, sunRiseColor,
                    0.5f + daytime / (float) Skybox.riseSetDuration).toGdx();
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - Skybox.riseSetDuration / 2) {
            return nullColor.toGdx();
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2) {
            return ClientWorld.mixColors(
                    sunSetColor, nullColor,
                    (daytime - ((double) ClientWorld.DAY_CYCLE / 2)) / Skybox.riseSetDuration).toGdx();
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + Skybox.riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    nullColor, sunSetColor,
                    (daytime - ((double) ClientWorld.DAY_CYCLE / 2 - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration).toGdx();
        } else if (daytime <= ClientWorld.DAY_CYCLE - Skybox.riseSetDuration / 2) {
            return nullColor.toGdx();
        } else {
            return ClientWorld.mixColors(
                    sunRiseColor, nullColor,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) Skybox.riseSetDuration / 2)) / Skybox.riseSetDuration).toGdx();
        }
    }
}
