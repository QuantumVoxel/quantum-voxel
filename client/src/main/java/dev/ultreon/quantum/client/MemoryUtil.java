package dev.ultreon.quantum.client;

import com.sun.jna.Native;

/**
 * Haha funny memory util.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class MemoryUtil {
    /**
     * Self destructs the program, because why not?
     * This is a joke, don't use it. Unless you want to crash the program. Or have a bad time.
     * <p>
     * This method frees the memory at 0x00000001, which is an invalid memory address and will crash the program with a segmentation fault.
     * {@code segmentation fault (core dumped)} 🤣
     */
    public static void selfDestruct() {
        Native.free(1L);
    }
}
