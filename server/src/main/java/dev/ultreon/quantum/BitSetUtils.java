package dev.ultreon.quantum;

import java.util.BitSet;

public class BitSetUtils {
    private BitSetUtils() { }

    public static byte[] toByteArray(BitSet bitSet) {
        byte[] bytes = new byte[bitSet.length() / 8 + 1];
        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) {
                bytes[i / 8] |= (byte) (1 << (i % 8));
            }
        }
        return bytes;
    }

    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bitSet = new BitSet(bytes.length * 8);
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                if ((bytes[i] & (1 << j)) != 0) {
                    bitSet.set(i * 8 + j);
                }
            }
        }
        return bitSet;
    }
}
