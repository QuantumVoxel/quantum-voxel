package dev.ultreon.hydro.util;

public class MathUtils {
    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double sin(double angle) {
        return Math.sin(angle);
    }

    public static double cos(double angle) {
        return Math.cos(angle);
    }

    public static double sqrt(double value) {
        return Math.sqrt(value);
    }

    public static int round(double value) {
        return (int) Math.round(value);
    }

    public static int round(float value) {
        return Math.round(value);
    }
}
