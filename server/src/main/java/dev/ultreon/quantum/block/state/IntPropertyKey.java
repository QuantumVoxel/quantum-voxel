package dev.ultreon.quantum.block.state;

import dev.ultreon.quantum.network.PacketIO;
import org.jetbrains.annotations.NotNull;

public class IntPropertyKey extends StatePropertyKey<Integer> {
    private final int minValue;
    private final int maxValue;

    public IntPropertyKey(String name, int minValue, int maxValue) {
        super(name, createValues(minValue, maxValue), Integer.class);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private static Integer[] createValues(int minValue, int maxValue) {
        Integer[] values = new Integer[maxValue - minValue + 1];
        for (int i = minValue; i <= maxValue; i++) {
            values[i - minValue] = i;
        }
        return values;
    }

    @Override
    public Integer read(@NotNull PacketIO packetBuffer) {
        return packetBuffer.readInt();
    }

    @Override
    public void write(@NotNull PacketIO packetBuffer, Object value) {
        if (value instanceof Integer integer) {
            packetBuffer.writeInt(integer);
        }
    }
}
