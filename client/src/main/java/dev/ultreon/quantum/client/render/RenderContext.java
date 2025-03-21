package dev.ultreon.quantum.client.render;

/**
 * Represents a context used for rendering operations.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @param <T> The type of the holder object associated with this render context.
 */
public interface RenderContext<T> {
    /**
     * Returns the holder object associated with this render context.
     * <p>
     * This is used to get the holder object associated with this render context.
     * </p>
     * 
     * @return the holder object of type T.
     */
    T getHolder();

    RenderBufferSource getBufferSource();
}
