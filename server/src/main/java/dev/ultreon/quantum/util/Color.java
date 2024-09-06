package dev.ultreon.quantum.util;

public interface Color {
    /**
     * @return The red component
     */
    int getRed();

    /**
     * @return The green component
     */
    int getGreen();

    /**
     * @return The blue component
     */
    int getBlue();

    /**
     * @return The alpha component
     */
    int getAlpha();

    default com.badlogic.gdx.graphics.Color toGdx() {
        return new com.badlogic.gdx.graphics.Color(getRed() / 255f, getGreen() / 255f, getBlue() / 255f, getAlpha() / 255f);
    }

    static Color fromGdx(com.badlogic.gdx.graphics.Color color) {
        return RgbColor.gdx(color);
    }

    default Color darker() {
        return RgbColor.gdx(toGdx()).darker();
    }

    default Color lighter() {
        return RgbColor.gdx(toGdx()).lighter();
    }

    default String toHex() {
        return String.format("#%02x%02x%02x%02x", getRed(), getGreen(), getBlue(), getAlpha());
    }

    default Color withRed(int i) {
        return new RgbColor(i / 255f, getGreen(), getBlue(), getAlpha());
    }

    default Color withGreen(int i) {
        return new RgbColor(getRed(), i / 255f, getBlue(), getAlpha());
    }

    default Color withBlue(int i) {
        return new RgbColor(getRed(), getGreen(), i / 255f, getAlpha());
    }

    default Color withAlpha(int i) {
        return new RgbColor(getRed(), getGreen(), getBlue(), i / 255f);
    }
}
