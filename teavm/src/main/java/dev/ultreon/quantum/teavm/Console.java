package dev.ultreon.quantum.teavm;

import org.teavm.jso.JSBody;

public class Console {
    @JSBody(script = "return console.log(message);", params = {"message"})
    public static native void log(String message);
    
    @JSBody(script = "return console.trace(message);", params = {"message"})
    public static native void trace(String message);

    @JSBody(script = "return console.debug(message);", params = {"message"})
    public static native void debug(String message);

    @JSBody(script = "return console.info(message);", params = {"message"})
    public static native void info(String message);

    @JSBody(script = "return console.warn(message);", params = {"message"})
    public static native void warn(String message);

    @JSBody(script = "return console.error(message);", params = {"message"})
    public static native void error(String message);
    
    @JSBody(script = "if(e.$jsException && e.$jsException.stack) { return console.log(message, e.$jsException.stack); } else if(e) { return console.log(message, e); } else { return console.log(message, e); }", params = {"message", "e"})
    public static native void log(String message, Object e);

    @JSBody(script = "if(e.$jsException && e.$jsException.stack) { return console.trace(message, e.$jsException.stack); } else if(e) { return console.trace(message, e); } else { return console.trace(message, e); }", params = {"message", "e"})
    public static native void trace(String message, Object e);

    @JSBody(script = "if(e.$jsException && e.$jsException.stack) { return console.debug(message, e.$jsException.stack); } else if(e) { return console.debug(message, e); } else { return console.debug(message, e); }", params = {"message", "e"})
    public static native void debug(String message, Object e);

    @JSBody(script = "if(e.$jsException && e.$jsException.stack) { return console.info(message, e.$jsException.stack); } else if(e) { return console.info(message, e); } else { return console.info(message); }", params = {"message", "e"})
    public static native void info(String message, Object e);

    @JSBody(script = "if(e.$jsException && e.$jsException.stack) { return console.warn(message, e.$jsException.stack); } else if(e) { return console.warn(message, e); } else { return console.warn(message); }", params = {"message", "e"})
    public static native void warn(String message, Object e);

    @JSBody(script = "if(e.$jsException && e.$jsException.stack) { return console.error(message, e.$jsException.stack); } else if(e) { return console.error(message, e); } else { return console.error(message); }", params = {"message", "e"})
    public static native void error(String message, Object e);
}
