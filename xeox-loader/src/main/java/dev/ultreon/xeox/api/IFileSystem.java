package dev.ultreon.xeox.api;

public interface IFileSystem {
    IPath root();

    IPath path(String path);

    IPath path(String first, String... more);

    boolean isReadOnly();

    default boolean isProtected(String path) {
        return false;
    }
}
