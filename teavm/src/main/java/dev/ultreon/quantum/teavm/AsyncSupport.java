package dev.ultreon.quantum.teavm;

import org.teavm.jso.JSBody;

public class AsyncSupport {
    @JSBody(params = "r", script = "Promise.resolve().then(r);")
    static native void runAsync(Runnable r);
}
