package dev.ultreon.quantum.util;

public class OutOfRangeException extends RuntimeException {
    public OutOfRangeException(int index, int i, int totalSize) {
        super("Index " + index + " is out of bounds for length " + totalSize + " (expected: range(" + i + ", " + (i + 1) + "))");
    }
}
