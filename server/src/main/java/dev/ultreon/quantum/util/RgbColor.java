package dev.ultreon.quantum.util;

import dev.ultreon.libs.commons.v0.exceptions.InvalidValueException;
import dev.ultreon.quantum.text.ColorCode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public class RgbColor implements Color {
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
    private final java.awt.Color awtColor;

    private RgbColor(long red, long green, long blue, long alpha) {
        this.awtColor = new java.awt.Color((int) red, (int) green, (int) blue, (int) alpha);
    }

    private RgbColor(int red, int green, int blue, int alpha) {
        this.awtColor = new java.awt.Color(red, green, blue, alpha);
    }

    private RgbColor(@NotNull java.awt.Color color) {
        this.awtColor = color;
    }

    public RgbColor(float red, float green, float blue, float alpha) {
        this.awtColor = new java.awt.Color(red, green, blue, alpha);
    }

    public static @NotNull RgbColor hsb(float h, float s, float b) {
        return new RgbColor(java.awt.Color.getHSBColor(h, s, b));
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

    @ApiStatus.Internal
    public static @NotNull RgbColor awt(java.awt.Color awt) {
        return new RgbColor(awt);
    }

    public static @NotNull RgbColor gdx(com.badlogic.gdx.graphics.Color color) {
        return new RgbColor((int) (color.r * 255), (int) (color.g * 255), (int) (color.b * 255), (int) (color.a * 255));
    }

    public static @NotNull RgbColor of(ColorCode colorCode) {
        return RgbColor.rgb(colorCode.getColor());
    }

    public @NotNull java.awt.Color toAwt() {
        return this.awtColor;
    }

    public @NotNull RgbColor brighter() {
        return new RgbColor(this.awtColor.brighter());
    }

    public @NotNull RgbColor darker() {
        return new RgbColor(this.awtColor.darker());
    }

    @Override
    public int getRed() {
        return this.awtColor.getRed();
    }

    @Override
    public int getGreen() {
        return this.awtColor.getGreen();
    }

    @Override
    public int getBlue() {
        return this.awtColor.getBlue();
    }

    @Override
    public int getAlpha() {
        return this.awtColor.getAlpha();
    }

    public int getTransparency() {
        return this.awtColor.getTransparency();
    }

    public int getRgb() {
        return this.awtColor.getRGB();
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
