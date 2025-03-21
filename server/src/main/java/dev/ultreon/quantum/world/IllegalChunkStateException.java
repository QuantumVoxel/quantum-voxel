package dev.ultreon.quantum.world;

/**
 * An error that is thrown when the world, region or chunk is in an illegal state.
 * This is a hard error, which would mean the game should crash.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class IllegalChunkStateException extends RuntimeException {
    public IllegalChunkStateException(String message) {
        super(message);
    }
}
