package dev.ultreon.quantum.block.state;

import dev.ultreon.libs.commons.v0.util.EnumUtils;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.StringType;

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

    @Override
    public int indexOf(T value) {
        return value.ordinal();
    }

    @Override
    public void load(BlockState blockState, DataType<?> value) {
        blockState.with(this, EnumUtils.byName((String) value.getValue(), allPossibleValues().getFirst()));
    }

    @Override
    public DataType<?> save(BlockState blockState) {
        return new StringType(blockState.get(this).name());
    }
}
