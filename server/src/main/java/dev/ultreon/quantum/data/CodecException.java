package dev.ultreon.quantum.data;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class CodecException extends GdxRuntimeException {
    public CodecException(String message) {
        super(message);
    }

    public CodecException(Throwable t) {
        super(t);
    }

    public CodecException(String message, Throwable t) {
        super(message, t);
    }

}
