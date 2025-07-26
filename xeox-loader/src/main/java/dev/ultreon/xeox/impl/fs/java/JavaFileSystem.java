package dev.ultreon.xeox.impl.fs.java;

import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;

import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.util.Set;

public class JavaFileSystem implements IFileSystem, AutoCloseable {
    private final FileSystem fileSystem;
    private final Set<String> protectedPaths;

    public JavaFileSystem(FileSystem fileSystem) {
        this(fileSystem, Set.of());
    }

    public JavaFileSystem(FileSystem fileSystem, String protectedPath) {
        this(fileSystem, Set.of(protectedPath));
    }

    public JavaFileSystem(FileSystem fileSystem, String... protectedPaths) {
        this(fileSystem, Set.of(protectedPaths));
    }

    public JavaFileSystem(FileSystem fileSystem, Set<String> protectedPaths) {
        this.fileSystem = fileSystem;
        this.protectedPaths = protectedPaths;
    }

    public static IFileSystem getDefault() {
        try {
            return new IFileSystem() {
                @Override
                public IPath root() {
                    return new JavaPath(this, Paths.get("/"));
                }

                @Override
                public IPath path(String path) {
                    return new JavaPath(this, Paths.get(path));
                }

                @Override
                public IPath path(String first, String... more) {
                    return new JavaPath(this, Paths.get(first, more));
                }

                @Override
                public boolean isReadOnly() {
                    return false;
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Could not get default file system", e);
        }
    }

    @Override
    public IPath root() {
        return new JavaPath(this, fileSystem.getPath("/"));
    }

    @Override
    public IPath path(String path) {
        return new JavaPath(this, fileSystem.getPath(path));
    }

    @Override
    public IPath path(String first, String... more) {
        return new JavaPath(this, fileSystem.getPath(first, more));
    }

    @Override
    public boolean isReadOnly() {
        return fileSystem.isReadOnly();
    }

    @Override
    public boolean isProtected(String path) {
        for (String protectedPath : protectedPaths) {
            if (path(path).startsWith(protectedPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void close() throws Exception {
        fileSystem.close();
    }

    @Override
    public String toString() {
        return "JavaFileSystem{" +
                "fileSystem=" + fileSystem +
                ", protectedPaths=" + protectedPaths +
                '}';
    }
}
