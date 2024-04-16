package com.ultreon.craft.network;

public class MemoryConnectionContext {
    private static MemoryConnection memoryConnection;

    public static MemoryConnection get() {
        return memoryConnection;
    }

    public static void set(MemoryConnection memoryConnection) {
        MemoryConnectionContext.memoryConnection = memoryConnection;
    }
}
