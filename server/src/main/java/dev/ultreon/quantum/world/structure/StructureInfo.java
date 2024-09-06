package dev.ultreon.quantum.world.structure;

import kotlin.ranges.IntRange;

public record StructureInfo(
        Structure structure,
        float chance,
        IntRange heightRange
) {
}
