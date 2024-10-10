package dev.ultreon.quantum.block.state;

public class EnumPropertyKey<T extends Enum<T> & StringSerializable> extends StatePropertyKey<T> {
    public EnumPropertyKey(String name, Class<T> enumClass) {
        super(name, createValues(enumClass), enumClass);
    }

    @SafeVarargs
    public EnumPropertyKey(String name, Class<T> enumClass, T... values) {
        super(name, values, enumClass);
    }

    private static <T extends Enum<T> & StringSerializable> T[] createValues(Class<T> enumClass) {
        return enumClass.getEnumConstants();
    }

}
