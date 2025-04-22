package dev.ultreon.quantum.world.structure;

import kotlin.ranges.IntRange;

import java.util.Objects;

public final class StructureInfo {
    private final Structure structure;
    private final float chance;
    private final IntRange heightRange;

    public StructureInfo(
            Structure structure,
            float chance,
            IntRange heightRange
    ) {
        this.structure = structure;
        this.chance = chance;
        this.heightRange = heightRange;
    }

    public Structure structure() {
        return structure;
    }

    public float chance() {
        return chance;
    }

    public IntRange heightRange() {
        return heightRange;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StructureInfo) obj;
        return Objects.equals(this.structure, that.structure) &&
               Float.floatToIntBits(this.chance) == Float.floatToIntBits(that.chance) &&
               Objects.equals(this.heightRange, that.heightRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure, chance, heightRange);
    }

    @Override
    public String toString() {
        return "StructureInfo[" +
               "structure=" + structure + ", " +
               "chance=" + chance + ", " +
               "heightRange=" + heightRange + ']';
    }

}
