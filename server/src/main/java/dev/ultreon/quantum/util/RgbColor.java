package dev.ultreon.quantum.util;

import dev.ultreon.libs.commons.v0.exceptions.InvalidValueException;
import dev.ultreon.quantum.text.ColorCode;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public class RgbColor implements Color {
    private static final double FACTOR = 0.7;

    public static final RgbColor BLACK = RgbColor.rgb(0x000000);
    public static final RgbColor DARK_GRAY = RgbColor.rgb(0x404040);
    public static final RgbColor GRAY = RgbColor.rgb(0x808080);
    public static final RgbColor LIGHT_GRAY = RgbColor.rgb(0xb0b0b0);
    public static final RgbColor WHITE = RgbColor.rgb(0xffffff);
    public static final RgbColor RED = RgbColor.rgb(0xff0000);
    public static final RgbColor ORANGE = RgbColor.rgb(0xff8000);
    public static final RgbColor GOLD = RgbColor.rgb(0xffb000);
    public static final RgbColor YELLOW = RgbColor.rgb(0xffff00);
    public static final RgbColor YELLOW_GREEN = RgbColor.rgb(0x80ff00);
    public static final RgbColor GREEN = RgbColor.rgb(0x00ff00);
    public static final RgbColor MINT = RgbColor.rgb(0x00ff80);
    public static final RgbColor CYAN = RgbColor.rgb(0x00ffff);
    public static final RgbColor AZURE = RgbColor.rgb(0x0080ff);
    public static final RgbColor BLUE = RgbColor.rgb(0x0000ff);
    public static final RgbColor PURPLE = RgbColor.rgb(0x8000ff);
    public static final RgbColor MAGENTA = RgbColor.rgb(0xff00ff);
    public static final RgbColor ROSE = RgbColor.rgb(0xff0080);
    public static final RgbColor TRANSPARENT = RgbColor.rgba(0x00000000);

    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    private RgbColor(long red, long green, long blue, long alpha) {
        this.red = (int) red;
        this.green = (int) green;
        this.blue = (int) blue;
        this.alpha = (int) alpha;
    }

    private RgbColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public RgbColor(float red, float green, float blue, float alpha) {
        this((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    public static @NotNull RgbColor hsb(float h, float s, float b) {
        float[] rgb = hsb2rgb(h, s, b);
        return new RgbColor((int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255), 255);
    }

    private static float[] hsb2rgb(float h, float s, float b) {
        float[] rgb = new float[3];
        float c = b * s;
        float x = c * (1 - Math.abs((h / 60) % 2 - 1));
        float m = b - c;
        if (h < 60) {
            rgb[0] = c + m;
            rgb[1] = x + m;
            rgb[2] = 0 + m;
        } else if (h < 120) {
            rgb[0] = x + m;
            rgb[1] = c + m;
            rgb[2] = 0 + m;
        } else if (h < 180) {
            rgb[0] = 0 + m;
            rgb[1] = c + m;
            rgb[2] = x + m;
        }

        if (h < 240) {
            rgb[0] = 0 + m;
            rgb[1] = x + m;
            rgb[2] = c + m;
        } else if (h < 300) {
            rgb[0] = x + m;
            rgb[1] = 0 + m;
            rgb[2] = c + m;
        } else if (h < 360) {
            rgb[0] = c + m;
            rgb[1] = 0 + m;
            rgb[2] = x + m;
        } else {
            rgb[0] = c + m;
            rgb[1] = x + m;
            rgb[2] = 0 + m;
        }

        return rgb;
    }

    public static @NotNull RgbColor rgb(int red, int green, int blue) {
        return new RgbColor(red, green, blue, 255);
    }

    public static @NotNull RgbColor rgb(float red, float green, float blue) {
        return new RgbColor((int) (red * 255), (int) (green * 255), (int) (blue * 255), 255);
    }

    public static @NotNull RgbColor rgba(int red, int green, int blue, int alpha) {
        return new RgbColor(red, green, blue, alpha);
    }

    public static @NotNull RgbColor rgba(float red, float green, float blue, float alpha) {
        return new RgbColor((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    public static @NotNull RgbColor rgb(int color) {
        long rgb = ((long) color) % 0x100000000L;
        return new RgbColor((rgb & 0xff0000L) >> 16, (rgb & 0x00ff00L) >> 8, rgb & 0x0000ffL, 255);
    }

    public static @NotNull RgbColor rgba(int color) {
        long rgba = ((long) color) % 0x100000000L;
        return new RgbColor((rgba & 0xff000000L) >> 24, (rgba & 0x00ff0000L) >> 16, (rgba & 0x0000ff00L) >> 8, rgba & 0x000000ffL);
    }

    public static @NotNull RgbColor argb(int color) {
        long argb = ((long) color) % 0x100000000L;
        return new RgbColor((argb & 0x00ff0000L) >> 16, (argb & 0x0000ff00L) >> 8, argb & 0x000000ffL, (argb & 0xff000000L) >> 24);
    }

    public static @NotNull RgbColor bgr(int color) {
        long bgr = ((long) color) % 0x100000000L;
        return new RgbColor(bgr & 0x0000ffL, (bgr & 0x00ff00L) >> 8, (bgr & 0xff0000L) >> 16, 255);
    }

    public static @NotNull RgbColor bgra(int color) {
        long bgra = ((long) color) % 0x100000000L;
        return new RgbColor((bgra & 0x0000ff00L) >> 8, (bgra & 0x00ff0000L) >> 16, (bgra & 0xff000000L) >> 24, bgra & 0x000000ffL);
    }

    public static @NotNull RgbColor abgr(int color) {
        long abgr = ((long) color) % 0x100000000L;
        return new RgbColor(abgr & 0x000000ffL, (abgr & 0x0000ff00L) >> 8, (abgr & 0x00ff0000L) >> 16, (abgr & 0xff000000L) >> 24);
    }

    public static @NotNull RgbColor hex(String hex) {
        if (Pattern.matches("#[0-9a-fA-F]{6}", hex)) {
            int rgb = Integer.valueOf(hex.substring(1), 16);
            return RgbColor.rgb(rgb);
        } else if (Pattern.matches("#[0-9a-fA-F]{8}", hex)) {
            int rgb = Integer.parseUnsignedInt(hex.substring(1), 16);
            return RgbColor.rgba(rgb);
        } else if (Pattern.matches("#[0-9a-fA-F]{3}", hex)) {
            int rgb = Integer.valueOf(new String(new char[]{
                    hex.charAt(1), hex.charAt(1),
                    hex.charAt(2), hex.charAt(2),
                    hex.charAt(3), hex.charAt(3)}), 16);
            return RgbColor.rgb(rgb);
        } else if (Pattern.matches("#[0-9a-fA-F]{4}", hex)) {
            int rgb = Integer.valueOf(new String(new char[]{
                    hex.charAt(1), hex.charAt(1),
                    hex.charAt(2), hex.charAt(2),
                    hex.charAt(3), hex.charAt(3),
                    hex.charAt(4), hex.charAt(4)}), 16);
            return RgbColor.rgba(rgb);
        } else {
            if (!hex.isEmpty()) {
                if (hex.charAt(0) != '#') {
                    throw new InvalidValueException("First character create color code isn't '#'.");
                } else if (hex.length() != 3 && hex.length() != 4 && hex.length() != 6 && hex.length() != 8) {
                    throw new InvalidValueException("Invalid hex length, should be 3, 4, 6 or 8 in length.");
                } else {
                    throw new InvalidValueException("Invalid hex value. Hex values may only contain numbers and letters a to f.");
                }
            } else {
                throw new InvalidValueException("The color hex is empty, it should start with a hex, and then 3, 4, 6 or 8 hexadecimal digits.");
            }
        }
    }

    public static @NotNull RgbColor gdx(com.badlogic.gdx.graphics.Color color) {
        return new RgbColor((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255), (int) (color.a * 255));
    }

    public static @NotNull RgbColor of(ColorCode colorCode) {
        return RgbColor.rgb(colorCode.getColor());
    }

    public @NotNull RgbColor brighter() {
        int r = getRed();
        int g = getGreen();
        int b = getBlue();
        int alpha = getAlpha();

        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new RgbColor(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new RgbColor(Math.min((int) (r / FACTOR), 255),
                Math.min((int) (g / FACTOR), 255),
                Math.min((int) (b / FACTOR), 255),
                alpha);
    }

    public @NotNull RgbColor darker() {
        return new RgbColor(Math.max((int) (getRed() * FACTOR), 0),
                Math.max((int) (getGreen() * FACTOR), 0),
                Math.max((int) (getBlue() * FACTOR), 0),
                getAlpha());
    }

    @Override
    public int getRed() {
        return this.red;
    }

    @Override
    public int getGreen() {
        return this.green;
    }

    @Override
    public int getBlue() {
        return this.blue;
    }

    @Override
    public int getAlpha() {
        return this.alpha;
    }

    public int getTransparency() {
        return this.alpha;
    }

    public int getRgb() {
        return this.alpha << 24 | this.red << 16 | this.green << 8 | this.blue;
    }

    public @NotNull RgbColor withRed(int red) {
        return new RgbColor(red, this.getGreen(), this.getBlue(), this.getAlpha());
    }

    public @NotNull RgbColor withGreen(int green) {
        return new RgbColor(this.getRed(), green, this.getBlue(), this.getAlpha());
    }

    public @NotNull RgbColor withBlue(int blue) {
        return new RgbColor(this.getRed(), this.getGreen(), blue, this.getAlpha());
    }

    public @NotNull RgbColor withAlpha(int alpha) {
        return new RgbColor(this.getRed(), this.getGreen(), this.getBlue(), alpha);
    }

    @Override
    public String toString() {
        return String.format("#%02x%02x%02x%02x", this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha());
    }

    public com.badlogic.gdx.graphics.Color toGdx() {
        return new com.badlogic.gdx.graphics.Color(this.getRed() / 255f, this.getGreen() / 255f, this.getBlue() / 255f, this.getAlpha() / 255f);
    }
}
