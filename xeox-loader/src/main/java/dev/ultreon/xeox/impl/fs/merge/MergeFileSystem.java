package dev.ultreon.xeox.impl.fs.merge;

import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;

import java.util.ArrayList;
import java.util.List;

public class MergeFileSystem implements IFileSystem {
    private final List<IFileSystem> fileSystems;

    public MergeFileSystem(List<IFileSystem> fileSystems) {
        this.fileSystems = fileSystems;
    }

    @Override
    public IPath root() {
        List<IPath> paths = new ArrayList<>();
        for (IFileSystem fileSystem : fileSystems) {
            paths.add(fileSystem.root());
        }
        
        return new MergePath(this, paths);
    }

    @Override
    public IPath path(String path) {
        List<IPath> paths = new ArrayList<>();
        for (IFileSystem fileSystem : fileSystems) {
            paths.add(fileSystem.path(path.startsWith("/") ? path.substring(1) : path));
        }
        
        return new MergePath(this, paths);
    }

    @Override
    public IPath path(String first, String... more) {
        return path(first).child(more);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
