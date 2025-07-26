package dev.ultreon.xeox.impl.fs.merge;

import dev.ultreon.xeox.api.IPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MergePath implements IPath {
    private final MergeFileSystem fs;
    private final List<IPath> paths;

    public MergePath(MergeFileSystem fs, List<IPath> paths) {
        this.fs = fs;
        this.paths = paths;

        for (IPath path : paths) {
            if (path.path().startsWith("//")) {
                throw new IllegalArgumentException("What does // mean? " + path.path() + " is not a valid path");
            }
        }
    }

    @Override
    public String path() {
        return paths.get(0).path();
    }

    @Override
    public IPath parent() {
        List<IPath> parentPaths = new ArrayList<>();
        for (IPath path : paths) {
            parentPaths.add(path.parent());
        }
        return new MergePath(fs, parentPaths);
    }

    @Override
    public IPath child(String name) {
        List<IPath> childPaths = new ArrayList<>();
        for (IPath path : paths) {
            childPaths.add(path.child(name));
        }
        return new MergePath(fs, childPaths);
    }

    @Override
    public IPath child(String first, String... more) {
        List<IPath> childPaths = new ArrayList<>();
        for (IPath path : paths) {
            childPaths.add(path.child(first, more));
        }
        return new MergePath(fs, childPaths);
    }

    @Override
    public IPath sibling(String name) {
        List<IPath> siblingPaths = new ArrayList<>();
        for (IPath path : paths) {
            siblingPaths.add(path.sibling(name));
        }
        return new MergePath(fs, siblingPaths);
    }

    @Override
    public boolean exists() {
        for (IPath path : paths) {
            if (path.exists()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDirectory() {
        for (IPath path : paths) {
            if (path.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFile() {
        for (IPath path : paths) {
            if (path.isFile()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean create() throws IOException {
        for (IPath path : paths) {
            try {
                path.create();
                return true;
            } catch (Throwable e) {
                // Ignore
            }
        }
        
        return false;
    }

    @Override
    public boolean createDirectory() throws IOException {
        for (IPath path : paths) {
            try {
                path.createDirectory();
                return true;
            } catch (Throwable e) {
                // Ignore
            }
        }
        
        return false;
    }

    @Override
    public boolean delete() throws IOException {
        for (IPath path : paths) {
            try {
                path.delete();
                return true;
            } catch (Throwable e) {
                // Ignore
            }
        }
        
        return false;
    }

    @Override
    public boolean rename(String newName) throws IOException {
        for (IPath path : paths) {
            try {
                path.rename(newName);
                return true;
            } catch (Throwable e) {
                // Ignore
            }
        }
        
        return false;
    }

    @Override
    public void copy(IPath destination, Consumer<Throwable> onError) {
        for (IPath path : paths) {
            try {
                path.copy(destination, onError);
                return;
            } catch (Throwable e) {
                onError.accept(e);
            }
        }
    }

    @Override
    public String[] list() throws IOException {
        Set<String> list = new HashSet<>();
        for (IPath path : paths) {
            try {
                list.addAll(List.of(path.list()));
            } catch (Throwable e) {
                // Ignore
            }
        }
        return list.toArray(new String[0]);
    }

    @Override
    public String[] list(String extension) throws IOException {
        Set<String> list = new HashSet<>();
        for (IPath path : paths) {
            try {
                list.addAll(List.of(path.list(extension)));
            } catch (Throwable e) {
                // Ignore
            }
        }
        return list.toArray(new String[0]);
    }

    @Override
    public long lastModified() throws IOException {
        long lastModified = 0;
        for (IPath path : paths) {
            lastModified = Math.max(lastModified, path.lastModified());
        }
        return lastModified;
    }

    @Override
    public long length() throws IOException {
        for (IPath path : paths) {
            if (!path.exists()) {
                continue;
            }
            
            return path.length();
        }
        
        throw new IOException("Cannot stat any of the paths");
    }

    @Override
    public boolean isWritable() {
        for (IPath path : paths) {
            if (!path.exists()) {
                continue;
            }
            
            return path.isWritable();
        }
        
        return false;
    }

    @Override
    public boolean isReadable() {
        for (IPath path : paths) {
            if (!path.exists()) {
                continue;
            }
            
            return path.isReadable();
        }
        
        return false;
    }

    @Override
    public InputStream read() throws IOException {
        for (IPath path : paths) {
            if (!path.exists()) {
                continue;
            }
            
            return path.read();
        }
        
        throw new IOException("Cannot stat any of the paths");
    }

    @Override
    public OutputStream write(boolean append) throws IOException {
        for (IPath path : paths) {
            if (!path.exists()) {
                continue;
            }
            
            return path.write(append);
        }
        
        throw new IOException("Cannot stat any of the paths");
    }

    @Override
    public ByteChannel channel() throws IOException {
        for (IPath path : paths) {
            if (!path.exists()) {
                continue;
            }
            
            return path.channel();
        }
        
        throw new IOException("Cannot stat any of the paths");
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) throws IOException {
        for (IPath path : paths) {
            if (!path.exists()) {
                continue;
            }

            return path.map(mode);
        }

        throw new IOException("Cannot stat any of the paths");
    }

    @Override
    public void move(IPath destination, Consumer<Throwable> onError) {
        for (IPath path : paths) {
            try {
                path.move(destination, onError);
                return;
            } catch (Throwable e) {
                onError.accept(e);
            }
        }
    }
}
