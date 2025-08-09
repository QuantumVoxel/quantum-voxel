package dev.ultreon.quantum.di;

/**
 * Exception thrown when a dependency cannot be resolved.
 */
public class DependencyResolutionException extends RuntimeException {
    /**
     * Creates a new dependency resolution exception.
     *
     * @param message the error message
     */
    public DependencyResolutionException(String message) {
        super(message);
    }

    /**
     * Creates a new dependency resolution exception.
     *
     * @param message the error message
     * @param cause the cause of the exception
     */
    public DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new dependency resolution exception.
     *
     * @param cause the cause of the exception
     */
    public DependencyResolutionException(Throwable cause) {
        super(cause);
    }
}