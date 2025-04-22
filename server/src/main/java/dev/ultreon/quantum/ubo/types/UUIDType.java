package dev.ultreon.quantum.ubo.types;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.ubo.DataTypes;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class UUIDType implements DataType<UUID> {
    private UUID obj;

    public UUIDType(UUID obj) {
        this.obj = obj;
    }

    @Override
    public UUID getValue() {
        return obj;
    }

    @Override
    public void setValue(UUID obj) {
        if (obj == null) throw new IllegalArgumentException("Value can't be set to null");
        this.obj = obj;
    }

    @Override
    public int id() {
        return DataTypes.UUID;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        long[] elements = GamePlatform.get().getUuidElements(obj);
        output.writeLong(elements[0]);
        output.writeLong(elements[1]);
    }

    public static UUIDType read(DataInput input) throws IOException {
        long msb = input.readLong();
        long lsb = input.readLong();
        return new UUIDType(GamePlatform.get().constructUuid(msb, lsb));
    }

    private static @NotNull String getUuid(long msb, long lsb) {
        String s = getPaddedHex(msb) + getPaddedHex(lsb);
        s = s.substring(0, 16) + "-" + s.substring(16, 32) + "-" + s.substring(32, 48) + "-" + s.substring(48, 64) + "-" + s.substring(64);
        return s;
    }

    private static @NotNull String getPaddedHex(long msb) {
        StringBuilder hexString = new StringBuilder(Long.toHexString(msb));
        for (int i = 0; i < 16 - hexString.length(); i++) {
            hexString.insert(0, "0");
        }
        return hexString.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UUIDType)) return false;
        UUIDType uuidType = (UUIDType) other;
        return Objects.equals(obj, uuidType.obj);
    }

    @Override
    public int hashCode() {
        return obj.hashCode();
    }

    @Override
    public UUIDType copy() {
        return new UUIDType(UUID.fromString(obj.toString()));
    }

    @Override
    public String writeUso() {
        return '<' + obj.toString() + '>';
    }

    @Override
    public String toString() {
        return writeUso();
    }
}
