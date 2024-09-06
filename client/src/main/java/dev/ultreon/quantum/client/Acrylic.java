package dev.ultreon.quantum.client;

public class Acrylic {
    public static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    public static final int DWMWA_MICA_EFFECT = 1029;
    public static final int DWMWA_SYSTEMBACKDROP_TYPE = 38;

    public static native boolean applyMica(long hwnd);

    public static native boolean applyTabbed(long hwnd);

    public static native boolean applyAcrylic(long hwnd);

    public static native boolean applyVibrancy(long hwnd);

    public static native boolean dwmSetWindowAttributes(long hwnd, int dwAttribute, int pvAttribute, int cbAttribute);

    static {
        System.loadLibrary("acrylic");
    }
}
