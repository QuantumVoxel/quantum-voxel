package dev.ultreon.quantum.teavm;

import org.teavm.jso.JSBody;

public final class UserAgent {
    private UserAgent() {
    }

    @JSBody(script = "return navigator.userAgent;")
    public static native String getUserAgent();

    public static boolean isDevAgent() {
        return getUserAgent().contains("QuantumVoxelDebug/1");
    }

    public static boolean isAndroid() {
        return getUserAgent().contains("Android");
    }

    public static boolean isIOS() {
        return getUserAgent().contains("iPhone") || getUserAgent().contains("iPad");
    }

    public static boolean isMobile() {
        return isAndroid() || isIOS();
    }
}