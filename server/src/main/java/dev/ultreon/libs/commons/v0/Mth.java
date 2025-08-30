package dev.ultreon.libs.commons.v0;

import com.badlogic.gdx.graphics.Color;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Math helper, for all your math needs.
 */
public class Mth {
    public static byte clamp(byte value, int min, int max) {
        if (value < min) return (byte) min;
        else return (byte) Math.min(value, max);
    }

    public static short clamp(short value, int min, int max) {
        if (value < min) return (short) min;
        else return (short) Math.min(value, max);
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) return min;
        else return Math.min(value, max);
    }

    public static long clamp(long value, long min, long max) {
        if (value < min) return min;
        else return Math.min(value, max);
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) return min;
        else return Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        else return Math.min(value, max);
    }

    public static BigInteger clamp(BigInteger value, BigInteger min, BigInteger max) {
        return value.max(min).min(max);
    }

    public static BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value.max(min).min(max);
    }

    public static double root(int value, int root) {
        return Math.pow(value, 1.0d / root);
    }

    public static double round(double value, int places) {
        if (((Double) value).isNaN() || ((Float) (float) value).isNaN()) {
            return value;
        }
        if (((Double) value).isInfinite() || ((Float) (float) value).isInfinite()) {
            return value;
        }

        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double lerp(double min, double max, double percentage) {
        return min + percentage * (max - min);
    }

    public static Color mixColors(Color color1, Color color2, double percent) {
        double inverse_percent = 1.0 - percent;
        float redPart = (float) (color1.r * percent + color2.r * inverse_percent);
        float greenPart = (float) (color1.g * percent + color2.g * inverse_percent);
        float bluePart = (float) (color1.b * percent + color2.b * inverse_percent);
        float alphaPart = (float) (color1.a * percent + color2.a * inverse_percent);
        return new Color(redPart, greenPart, bluePart, alphaPart);
    }

    public static byte diff(byte from, byte to) {
        return (byte) (Math.max(from, to) - Math.min (from, to));
    }

    public static short diff(short from, short to) {
        return (short) (Math.max(from, to) - Math.min (from, to));
    }

    public static int diff(int from, int to) {
        return Math.max(from, to) - Math.min (from, to);
    }

    public static long diff(long from, long to) {
        return Math.max(from, to) - Math.min (from, to);
    }

    public static float diff(float from, float to) {
        return Math.max(from, to) - Math.min (from, to);
    }

    public static double diff(double from, double to) {
        return Math.max(from, to) - Math.min (from, to);
    }
}
