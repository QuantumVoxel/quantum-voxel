package dev.ultreon.quantum.gamerule;

public class EnumUtils {
    public static Object getEnum(Class<? extends Enum> enumClass, String replace) {
        for (Enum e : enumClass.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(replace)) {
                return e;
            }
        }
        return null;
    }
}
