package dev.ultreon.quantum.client;

/**
 * Enum representing different game environments.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public enum GameEnvironment {
    /**
     * Represents the packaged game environment. E.g. using JPackage, or a native launcher.
     */
    PACKAGED,

    /**
     * Represents the development game environment. E.g. using an IDE.
     */
    DEVELOPMENT,

    /**
     * Represents the normal game environment. E.g. using a desktop launcher.
     */
    NORMAL,

    /**
     * Represents an unknown game environment. E.g. running the game directly from a jar file.
     */
    UNKNOWN
}
