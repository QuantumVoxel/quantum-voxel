package dev.ultreon.quantum.client.render;

/**
 * Represents a context used for rendering operations.
 *
 * @param <T> The type of the holder object associated with this render context.
 */
public interface RenderContext<T> {
    /**
     * Returns the holder object associated with this render context.
     *
     * @return the holder object of type T.
     */
    T getHolder();
}
