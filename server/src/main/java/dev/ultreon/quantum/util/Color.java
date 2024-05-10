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

    default java.awt.Color toAwt() {
        return new java.awt.Color(getRed(), getGreen(), getBlue(), getAlpha());
    }

    static Color fromAwt(java.awt.Color color) {
        return RgbColor.awt(color);
    }

    default Color darker() {
        return fromAwt(toAwt().darker());
    }

    default Color lighter() {
        return fromAwt(toAwt().brighter());
    }

    default String toHex() {
        return String.format("#%02x%02x%02x%02x", getRed(), getGreen(), getBlue(), getAlpha());
    }
}
