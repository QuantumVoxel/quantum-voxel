package dev.ultreon.quantum.teavm;

import org.teavm.jso.JSBody;

public class Console {

    @JSBody(script = "return console.debug(message, args);", params = {"message", "args"})
    public static native void debug(String message, Object... args);

    @JSBody(script = "return console.info(message, args);", params = {"message", "args"})
    public static native void info(String message, Object... args);

    @JSBody(script = "return console.warn(message, args);", params = {"message", "args"})
    public static native void warn(String message, Object... args);

    @JSBody(script = "return console.error(message, args);", params = {"message", "args"})
    public static native void error(String message, Object... args);

}
