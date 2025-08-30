package dev.ultreon.xeox.impl.fs.isolated;

import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;

import java.util.Set;

public class IsolatedFileSystem implements IFileSystem {
    final IPath parent;
    private final boolean readOnly;
    private final Set<String> protectedPaths;

    public IsolatedFileSystem(IPath parent, boolean readOnly) {
        this(parent, readOnly, dev.ultreon.quantum.SetUtils.of());
    }

    public IsolatedFileSystem(IPath parent, boolean readOnly, String... protectedPaths) {
        this(parent, readOnly, dev.ultreon.quantum.SetUtils.of(protectedPaths));
    }

    public IsolatedFileSystem(IPath parent, boolean readOnly, Set<String> protectedPaths) {
        this.parent = parent;
        this.readOnly = readOnly;
        this.protectedPaths = protectedPaths;
    }

    @Override
    public IPath root() {
        return new IsolatedPath(this, "");
    }

    @Override
    public IPath path(String path) {
        IPath located = root();
        if (path.startsWith("/")) {
            for (String part : path.substring(1).split("/")) {
                located = located.child(part);
            }
        } else for (String part : path.split("/")) {
            located = located.child(part);
        }
        return located;
    }

    @Override
    public IPath path(String first, String... more) {
        return path(first).child(more);
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
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
    public String toString() {
        return "IsolatedFileSystem{" +
                "parent=" + parent +
                ", readOnly=" + readOnly +
                ", protectedPaths=" + protectedPaths +
                '}';
    }
}
