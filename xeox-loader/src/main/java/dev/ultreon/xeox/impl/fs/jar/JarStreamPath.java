package dev.ultreon.xeox.impl.fs.jar;

import dev.ultreon.xeox.api.IPath;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.ReadOnlyFileSystemException;
import java.util.function.Consumer;

public class JarStreamPath implements IPath {
    private final JarStreamFileSystem fs;
    private final String[] path;

    JarStreamPath(JarStreamFileSystem fs, String path, String... paths) {
        this.fs = fs;
        this.path = new String[paths.length + 1];
        this.path[0] = path;
        System.arraycopy(paths, 0, this.path, 1, paths.length);
    }

    @Override
    public String path() {
        return "/" + dev.ultreon.quantum.StringUtils.join("/", this.path);
    }

    @Override
    public IPath parent() {
        if (this.path.length == 1) {
            return this.fs.root();
        } else {
            String[] parentPath = new String[this.path.length - 1];
            System.arraycopy(this.path, 0, parentPath, 0, parentPath.length);
            return new JarStreamPath(this.fs, dev.ultreon.quantum.StringUtils.join("/", parentPath));
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
            return new JarStreamPath(this.fs, name.substring(1));
        } else {
            String[] childPath = new String[this.path.length + 1];
            System.arraycopy(this.path, 0, childPath, 0, this.path.length);
            childPath[childPath.length - 1] = name;
            return new JarStreamPath(this.fs, dev.ultreon.quantum.StringUtils.join("/", childPath));
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
            return child(dev.ultreon.quantum.StringUtils.join("/", paths));
        }
    }

    @Override
    public IPath sibling(String name) {
        return parent().child(name);
    }

    @Override
    public boolean exists() {
        try {
            fs.channelFor(dev.ultreon.quantum.StringUtils.join("/", path)).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isDirectory() {
        return this.path.length == 1;
    }

    @Override
    public boolean isFile() {
        if (this.path.length == 1) {
            return false;
        } else {
            try {
                fs.channelFor(dev.ultreon.quantum.StringUtils.join("/", path)).close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    @Override
    public boolean create() {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public boolean createDirectory() {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public boolean delete() {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public boolean rename(String newName) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void move(IPath destination, Consumer<Throwable> onError) {
        throw new ReadOnlyFileSystemException();
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
            byte[] buffer = new byte[1024];
            int read;

            try (InputStream in = read(); OutputStream out = destination.write(false)) {
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            } catch (IOException e) {
                onError.accept(e);
                return;
            }
        }

        if (!destination.exists()) {
            onError.accept(new IOException("Failed to copy file"));
        }
    }

    @Override
    public String[] list() throws IOException {
        return new String[0];
    }

    @Override
    public String[] list(String extension) throws IOException {
        return new String[0];
    }

    @Override
    public long lastModified() {
        return 0L;
    }

    @Override
    public long length() {
        if (this.path.length == 1) {
            return 0L;
        } else {
            try (SeekableByteChannel seekableByteChannel = fs.channelFor(dev.ultreon.quantum.StringUtils.join("/", path))) {
                return seekableByteChannel.size();
            } catch (IOException e) {
                return 0L;
            }
        }
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return exists();
    }

    @Override
    public InputStream read() {
        try {
            return fs.streamFor(dev.ultreon.quantum.StringUtils.join("/", path));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public OutputStream write(boolean append) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public ByteChannel channel() {
        try {
            return fs.channelFor(dev.ultreon.quantum.StringUtils.join("/", path));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public @Nullable SeekableByteChannel seekableChannel() {
        try {
            return fs.channelFor(dev.ultreon.quantum.StringUtils.join("/", path));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) throws IOException {
        long length = length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large");
        }
        ByteBuffer buffer = ByteBuffer.allocate((int) length);
        try (ByteChannel channel = channel()) {
            channel.read(buffer);
        }
        buffer.flip();
        return buffer;
    }

    @Override
    public String toString() {
        return path();
    }
}
