package dev.ultreon.xeox.impl.fs.isolated;

import dev.ultreon.xeox.api.IPath;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.util.function.Consumer;

public class IsolatedPath implements IPath {
    private final IsolatedFileSystem fs;
    private final String[] path;

    IsolatedPath(IsolatedFileSystem fs, String path, String... paths) {
        this.fs = fs;
        this.path = new String[paths.length + 1];
        this.path[0] = path;
        System.arraycopy(paths, 0, this.path, 1, paths.length);
    }

    public static IPath of(IPath root) {
        return new IsolatedPath(new IsolatedFileSystem(root, false), "");
    }

    public static IPath of(IPath root, boolean readOnly) {
        return new IsolatedPath(new IsolatedFileSystem(root, readOnly), "");
    }

    public static IPath of(IPath root, String path, String... paths) {
        return new IsolatedPath(new IsolatedFileSystem(root, false), path, paths);
    }

    public static IPath of(IPath root, boolean readOnly, String path, String... paths) {
        return new IsolatedPath(new IsolatedFileSystem(root, readOnly), path, paths);
    }

    @Override
    public String path() {
        return "/" + String.join("/", this.path);
    }

    @Override
    public IPath parent() {
        if (this.path.length == 1) {
            return this.fs.root();
        } else {
            String[] parentPath = new String[this.path.length - 1];
            System.arraycopy(this.path, 0, parentPath, 0, parentPath.length);
            return new IsolatedPath(this.fs, String.join("/", parentPath));
        }
    }

    @Override
    public IPath child(String name) {
        if (name.isEmpty() || name.equals(".")) return this;
        if (name.equals("..")) return parent();

        if (name.contains("/")) {
            IPath current = this;
            for (String part : name.split("/")) {
                if (part.isEmpty() || part.equals(".")) continue;
                if (part.equals("..")) {
                    current = current.parent();
                    if (current == null)
                        return null;
                    continue;
                }
                current = current.child(part);
            }
            return current;
        }

        if (name.startsWith("/")) {
            return new IsolatedPath(this.fs, name.substring(1));
        } else {
            if (this.path.length == 1 && (path[0].isEmpty() || path[0].equals("/"))) {
                return new IsolatedPath(this.fs, name);
            }

            String[] childPath = new String[this.path.length + 1];
            System.arraycopy(this.path, 0, childPath, 0, this.path.length);
            childPath[childPath.length - 1] = name;
            return new IsolatedPath(this.fs, String.join("/", childPath));
        }
    }

    @Override
    public IPath child(String first, String... more) {
        if (more.length == 0) {
            return child(first);
        } else {
            String[] paths = new String[more.length + 1];
            paths[0] = first;
            System.arraycopy(more, 0, paths, 1, more.length);
            return child(String.join("/", paths));
        }
    }

    @Override
    public IPath sibling(String name) {
        return parent().child(name);
    }

    @Override
    public boolean exists() {
        return fs.parent.child(path).exists();
    }

    @Override
    public boolean isDirectory() {
        if (!exists()) return false;

        if (this.path.length == 1) {
            return true;
        } else {
            return fs.parent.child(path).isDirectory();
        }
    }

    @Override
    public boolean isFile() {
        if (!exists()) return false;

        if (this.path.length == 1) {
            return false;
        } else {
            return fs.parent.child(path).isFile();
        }
    }

    @Override
    public boolean create() throws IOException {
        if (fs.isReadOnly()) {
            return false;
        }

        return fs.parent.child(path).create();
    }

    @Override
    public boolean createDirectory() throws IOException {
        if (fs.isReadOnly()) {
            return false;
        }

        return fs.parent.child(path).createDirectory();
    }

    @Override
    public boolean delete() throws IOException {
        if (fs.isReadOnly()) {
            return false;
        }

        return fs.parent.child(path).delete();
    }

    @Override
    public boolean rename(String newName) throws IOException {
        if (fs.isReadOnly()) {
            return false;
        }

        return fs.parent.child(path).rename(newName);
    }

    @Override
    public void move(IPath destination, Consumer<Throwable> onError) {
        if (fs.isReadOnly()) {
            return;
        }

        if (destination.exists()) {
            onError.accept(new FileAlreadyExistsException(destination.path()));
            return;
        }

        if (isDirectory()) {
            try {
                destination.create();
            } catch (IOException e) {
                onError.accept(e);
                return;
            }
            forEachRecursive(path -> {
                if (path.isDirectory()) {
                    destination.child(path.path().substring(path.path().lastIndexOf("/") + 1)).create();
                } else {
                    path.copy(destination.child(path.path().substring(path.path().lastIndexOf("/") + 1)), onError);
                }
            }, onError::accept);

            return;
        }

        if (isFile()) {
            fs.parent.child(path).copy(destination, onError);
        }

    }

    @Override
    public void copy(IPath destination, Consumer<Throwable> onError) {
        if (destination.exists()) {
            onError.accept(new FileAlreadyExistsException(destination.path()));
            return;
        }

        if (isDirectory()) {
            try {
                destination.create();
            } catch (IOException e) {
                onError.accept(e);
                return;
            }
            forEachRecursive(path -> {
                if (path.isDirectory()) {
                    destination.child(path.path().substring(path.path().lastIndexOf("/") + 1)).create();
                } else {
                    path.copy(destination.child(path.path().substring(path.path().lastIndexOf("/") + 1)), onError);
                }
            }, onError::accept);

            return;
        }

        if (isFile()) {
            fs.parent.child(path).copy(destination, onError);
        }
    }

    @Override
    public String[] list() throws IOException {
        if (!exists())
            throw new FileNotFoundException(path());
        return fs.parent.child(path).list();
    }

    @Override
    public String[] list(String extension) throws IOException {
        if (!exists()) throw new FileNotFoundException(path());
        return fs.parent.child(path).list(extension);
    }

    @Override
    public long lastModified() throws IOException {
        if (!exists()) throw new FileNotFoundException(path());
        return fs.parent.child(path).lastModified();
    }

    @Override
    public long length() throws IOException {
        if (!exists()) throw new FileNotFoundException(path());
        return fs.parent.child(path).length();
    }

    @Override
    public boolean isWritable() {
        if (fs.isReadOnly()) {
            return false;
        }

        return fs.parent.child(path).isWritable();
    }

    @Override
    public boolean isReadable() {
        return fs.parent.child(path).isReadable();
    }

    @Override
    public InputStream read() throws IOException {
        return fs.parent.child(path).read();
    }

    @Override
    public OutputStream write(boolean append) throws IOException {
        return fs.parent.child(path).write(append);
    }

    @Override
    public ByteChannel channel() throws IOException {
        return fs.parent.child(path).channel();
    }

    @Override
    public @Nullable SeekableByteChannel seekableChannel() throws IOException {
        return fs.parent.child(path).seekableChannel();
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) throws IOException {
        return fs.parent.child(path).map(mode);
    }

    @Override
    public String toString() {
        return path();
    }
}
