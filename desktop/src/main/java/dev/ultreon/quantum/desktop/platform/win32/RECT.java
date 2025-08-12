package dev.ultreon.quantum.desktop.platform.win32;

import com.sun.jna.Structure;

// RECT structure for native calls
public class RECT extends Structure {
    public int left, top, right, bottom;

    @Override
    protected java.util.List<String> getFieldOrder() {
        return java.util.Arrays.asList("left", "top", "right", "bottom");
    }
}
