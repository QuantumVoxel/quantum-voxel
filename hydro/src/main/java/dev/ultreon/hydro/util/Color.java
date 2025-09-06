package dev.ultreon.hydro.util;

public class Color {
    public float r, g, b, a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public Color() {
        this(0, 0, 0, 1);
    }

    public Color set(float r, float g, float b, float a) {
        this.r = MathUtils.clamp(r, 0, 1);
        this.g = MathUtils.clamp(g, 0, 1);
        this.b = MathUtils.clamp(b, 0, 1);
        this.a = MathUtils.clamp(a, 0, 1);
        return this;
    }

    public Color set(float r, float g, float b) {
        return set(r, g, b, 1);
    }

    public Color set(Color other) {
        return set(other.r, other.g, other.b, other.a);
    }

    public Color setZero() {
        return set(0, 0, 0, 0);
    }

    public Color copy() {
        return new Color(r, g, b, a);
    }

    public Color add(Color other) {
        return set(r + other.r, g + other.g, b + other.b, a + other.a);
    }

    public Color sub(Color other) {
        return set(r - other.r, g - other.g, b - other.b, a - other.a);
    }

    public Color mul(Color other) {
        return set(r * other.r, g * other.g, b * other.b, a * other.a);
    }

    public Color mul(float scalar) {
        return set(r * scalar, g * scalar, b * scalar, a * scalar);
    }

    public Color div(Color other) {
        return set(r / other.r, g / other.g, b / other.b, a / other.a);
    }

    public Color div(float scalar) {
        return set(r / scalar, g / scalar, b / scalar, a / scalar);
    }

    public Color darken(float amount) {
        return mul(1 - amount);
    }

    public Color lighten(float amount) {
        return mul(1 + amount);
    }

    public Color invert() {
        r = 1 - r;
        g = 1 - g;
        b = 1 - b;
        a = 1 - a;
        return this;
    }

    public Color clamp(Color min, Color max) {
        r = MathUtils.clamp(r, min.r, max.r);
        g = MathUtils.clamp(g, min.g, max.g);
        b = MathUtils.clamp(b, min.b, max.b);
        a = MathUtils.clamp(a, min.a, max.a);
        return this;
    }

    public Color clamp(float min, float max) {
        r = MathUtils.clamp(r, min, max);
        g = MathUtils.clamp(g, min, max);
        b = MathUtils.clamp(b, min, max);
        a = MathUtils.clamp(a, min, max);
        return this;
    }

    public Color lerp(Color target, float alpha) {
        return set(r + (target.r - r) * alpha, g + (target.g - g) * alpha, b + (target.b - b) * alpha, a + (target.a - a) * alpha);
    }

    public Color lerp(Color target, float alpha, Color output) {
        return output.set(r + (target.r - r) * alpha, g + (target.g - g) * alpha, b + (target.b - b) * alpha, a + (target.a - a) * alpha);
    }

    public Color grayscale(float colorFactor) {
        float grey = 0.21f * r + 0.71f * g + 0.07f * b;
        return set(r * colorFactor + grey * (1.0f - colorFactor), g * colorFactor + grey * (1.0f - colorFactor), b * colorFactor + grey * (1.0f - colorFactor), 1.0f);
    }

    public Color clear() {
        return setZero();
    }

    public Color set(int color) {
        return set(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                ((color >> 24) & 0xFF) / 255f
        );
    }

    public int toInt() {
        return (int)(a * 255) << 24 | (int)(r * 255) << 16 | (int)(g * 255) << 8 | (int)(b * 255);
    }

    public String toString() {
        return String.format("##%02X%02X%02X%02X", (int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
    }

    public static Color BLACK = new Color(0, 0, 0);
    public static Color WHITE = new Color(1, 1, 1);
    public static Color RED = new Color(1, 0, 0);
    public static Color GREEN = new Color(0, 1, 0);
    public static Color BLUE = new Color(0, 0, 1);
    public static Color YELLOW = new Color(1, 1, 0);
    public static Color CYAN = new Color(0, 1, 1);
    public static Color MAGENTA = new Color(1, 0, 1);
    public static Color LIGHT_GRAY = new Color(0.75f, 0.75f, 0.75f);
    public static Color DARK_GRAY = new Color(0.25f, 0.25f, 0.25f);
    public static Color GRAY = new Color(0.5f, 0.5f, 0.5f);
    public static Color LIGHT_RED = new Color(1, 0.5f, 0.5f);
    public static Color LIGHT_GREEN = new Color(0.5f, 1, 0.5f);
    public static Color LIGHT_BLUE = new Color(0.5f, 0.5f, 1);
    public static Color LIGHT_YELLOW = new Color(1, 1, 0.5f);
    public static Color LIGHT_CYAN = new Color(0.5f, 1, 1);
    public static Color LIGHT_MAGENTA = new Color(1, 0.5f, 1);
    public static Color ORANGE = new Color(1, 0.647f, 0);
    public static Color PINK = new Color(1, 0.753f, 0.796f);
    public static Color PURPLE = new Color(0.627f, 0.125f, 0.941f);
    public static Color BROWN = new Color(0.545f, 0.271f, 0.074f);
    public static Color GREY = new Color(0.5f, 0.5f, 0.5f);
    public static Color DARK_RED = new Color(0.5f, 0f, 0f);
    public static Color DARK_GREEN = new Color(0f, 0.5f, 0f);
    public static Color DARK_BLUE = new Color(0f, 0f, 0.5f);
    public static Color DARK_YELLOW = new Color(0.5f, 0.5f, 0f);
    public static Color DARK_CYAN = new Color(0f, 0.5f, 0.5f);
    public static Color DARK_MAGENTA = new Color(0.5f, 0f, 0.5f);
    public static Color TRANSPARENT_BLACK = new Color(0, 0, 0, 0.5f);
    public static Color TRANSPARENT_WHITE = new Color(1, 1, 1, 0.5f);
    public static Color TRANSPARENT = new Color(0, 0, 0, 0);
}
