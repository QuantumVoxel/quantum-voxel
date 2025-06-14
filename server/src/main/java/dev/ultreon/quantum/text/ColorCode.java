package dev.ultreon.quantum.text;

import com.badlogic.gdx.utils.IntMap;
import dev.ultreon.quantum.util.Color;
import it.unimi.dsi.fastutil.chars.Char2ReferenceArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ReferenceMap;

import java.util.regex.Pattern;

public enum ColorCode implements Color {
    BLACK('0', 0, false, 0x000000),
    DARK_BLUE('1', 1, false, 0x0000aa),
    DARK_GREEN('2', 2, false, 0x00aa00),
    DARK_AQUA('3', 3, false, 0x00aaaa),
    DARK_RED('4', 4, false, 0xaa0000),
    DARK_PURPLE('5', 5, false, 0xaa00aa),
    GOLD('6', 6, false, 0xffaa00),
    GRAY('7', 7, false, 0xaaaaaa),
    DARK_GRAY('8', 8, false, 0x555555),
    BLUE('9', 9, false, 0x5555ff),
    GREEN('a', 10, false, 0x55ff55),
    AQUA('b', 11, false, 0x55ffff),
    RED('c', 12, false, 0xff5555),
    LIGHT_PURPLE('d', 13, false, 0xff55ff),
    YELLOW('e', 14, false, 0xffff55),
    WHITE('f', 15, false, 0xffffff),
    ORANGE('g', 16, false, 0xffaa55),
    MAGENTA('h', 17, false, 0xff55ff),
    AZURE('i', 18, false, 0x55aaff),
    LIME('j', 19, false, 0xaaff55),
    MAGIC('k', 16, true),
    BOLD('l', 17, true),
    STRIKETHROUGH('m', 18, true),
    UNDERLINE('n', 19, true),
    ITALIC('o', 20, true),
    RESET('r', 21);

    private final char code;
    private final int intCode;
    private final boolean isFormat;
    private final String toString;
    private final Integer color;

    ColorCode(char code, int intCode, boolean isFormat, Integer color) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.color = color;
        this.toString = new String(new char[]{'§', code});
    }

    ColorCode(char code, int intCode) {
        this(code, intCode, false);
    }

    ColorCode(char code, int intCode, boolean isFormat) {
        this(code, intCode, isFormat, null);
    }

    public char getCode() {
        return this.code;
    }

    public int getIntCode() {
        return this.intCode;
    }

    public boolean isFormat() {
        return this.isFormat;
    }

    public Integer getColor() {
        return this.color;
    }

    public boolean isColor() {
        return !this.isFormat && this != ColorCode.RESET;
    }

    @Override
    public String toString() {
        return this.toString;
    }

    public String concat(String str) {
        return this.toString + str;
    }

    public String concat(ColorCode color) {
        return this.toString + color.toString;
    }

    public ColorCode asBungee() {
        return ColorCode.getByChar(this.code);
    }

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

    private static final IntMap<ColorCode> BY_ID = new IntMap<>();
    private static final Char2ReferenceMap<ColorCode> BY_CHAR = new Char2ReferenceArrayMap<>();

    static {
        for (ColorCode color : ColorCode.values()) {
            ColorCode.BY_ID.put(color.intCode, color);
            ColorCode.BY_CHAR.put(color.code, color);
        }
    }

    public static ColorCode getByChar(char code) {
        return ColorCode.BY_CHAR.get(code);
    }

    public static ColorCode getById(int id) {
        return ColorCode.BY_ID.get(id);
    }

    public static ColorCode getByChar(String code) {
        return ColorCode.BY_CHAR.get(code.charAt(0));
    }

    public static String stripColor(final String input) {
        return input == null ? null : ColorCode.STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static ColorCode getLastColors(String input) {
        ColorCode result = ColorCode.WHITE;
        int length = input.length();

        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == '§' && index < length - 1) {
                char c = input.charAt(index + 1);
                ColorCode color = ColorCode.getByChar(c);

                if (color != null) {
                    result = color;

                    if (color.isColor() || color == ColorCode.RESET) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public int getRed() {
        return color >> 16 & 0xFF;
    }

    @Override
    public int getGreen() {
        return color >> 8 & 0xFF;
    }

    @Override
    public int getBlue() {
        return color & 0xFF;
    }

    @Override
    public int getAlpha() {
        return 255;
    }
}