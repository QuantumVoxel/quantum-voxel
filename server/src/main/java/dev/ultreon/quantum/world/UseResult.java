package dev.ultreon.quantum.world;

/**
 * UseResult represents the result of an attempted action, providing three possible outcomes:
 * {@link UseResult#ALLOW}, {@link UseResult#DENY}, and {@link UseResult#SKIP}.
 */
public enum UseResult {
    /**
     * Indicates that the use of the action is permitted.
     */
    ALLOW,

    /**
     * Indicates that the use of the action should be skipped.
     */
    SKIP,

    /**
     * Indicates that the use of the action should be denied.
     */
    DENY
}
