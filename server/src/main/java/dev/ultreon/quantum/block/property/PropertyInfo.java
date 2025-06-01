package dev.ultreon.quantum.block.property;

class PropertyInfo {
    final int offset;  // bit offset
    final int bits;    // number of bits
    final int mask;    // ((1 << bits) - 1) << offset

    PropertyInfo(int offset, int bits) {
        this.offset = offset;
        this.bits = bits;
        this.mask = ((1 << bits) - 1) << offset;
    }
}