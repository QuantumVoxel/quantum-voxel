package dev.ultreon.xeox.api;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public interface IPath {
    String path();

    IPath parent();

    IPath child(String name);

    IPath child(String first, String... more);

    IPath sibling(String name);

    boolean exists();

    boolean isDirectory();

    boolean isFile();

    boolean create() throws IOException;

    boolean createDirectory() throws IOException;

    default void createDirectories() throws IOException {
        IPath current = this;
        List<IPath> queue = new LinkedList<>();
        while (current.parent() != null) {
            queue.add(0, current);
            current = current.parent();
        }
        for (IPath path : queue) {
            if (!path.exists()) {
                if (!path.createDirectory()) {
                    return;
                }
            }
        }
    }

    boolean delete() throws IOException;

    boolean rename(String newName) throws IOException;

    void move(IPath destination, Consumer<Throwable> onError);

    void copy(IPath destination, Consumer<Throwable> onError);

    String[] list() throws IOException;

    String[] list(String extension) throws IOException;

    long lastModified() throws IOException;

    long length() throws IOException;

    boolean isWritable();

    boolean isReadable();

    InputStream read() throws IOException;

    default OutputStream write() throws IOException {
        return write(false);
    }

    OutputStream write(boolean append) throws IOException;

    default Reader reader() throws IOException {
        return new InputStreamReader(read());
    }

    default Reader reader(String encoding) throws IOException {
        return new InputStreamReader(read(), encoding);
    }

    default Writer writer() throws IOException {
        return new OutputStreamWriter(write());
    }

    default Writer writer(String encoding) throws IOException {
        return new OutputStreamWriter(write(), encoding);
    }

    default void forEach(IPathVisitor visitor, Consumer<IOException> onError) {
        if (isDirectory()) {
            try {
                for (String child : list()) {
                    try {
                        visitor.visit(child(child));
                    } catch (IOException e) {
                        onError.accept(e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                visitor.visit(this);
            } catch (IOException e) {
                onError.accept(e);
            }
        }
    }

    default void forEachRecursive(IPathVisitor visitor, Consumer<IOException> onError) {
        forEach(path -> {
            if (path.isDirectory()) {
                for (String child : path.list()) {
                    visitor.visit(path.child(child));
                    path.child(child).forEachRecursive(visitor, onError);
                }
            } else {
                visitor.visit(path);
            }
        }, onError);
    }

    default void deleteRecursive(Consumer<IOException> onError) {
        forEachRecursive(IPath::delete, onError);
    }

    default String readString() throws IOException {
        try (InputStream read = read()) {
            return new String(read.readAllBytes());
        }
    }

    default void writeString(String content) throws IOException {
        try (OutputStream write = write()) {
            write.write(content.getBytes());
        }
    }

    default byte[] readBytes() throws IOException {
        try (InputStream read = read()) {
            return read.readAllBytes();
        }
    }

    default void writeBytes(byte[] content) throws IOException {
        try (OutputStream write = write()) {
            write.write(content);
        }
    }

    ByteChannel channel() throws IOException;

    @Nullable
    default SeekableByteChannel seekableChannel() throws IOException {
        return null;
    }

    default void forEachFile(IPathVisitor visitor, Consumer<IOException> onError) {
        forEach(path -> {
            if (path.isFile()) {
                visitor.visit(path);
            }
        }, onError);
    }

    default IPath child(String[] path) {
        IPath current = this;
        for (int i = 0; i < path.length; i++) {
            String p = path[i];
            if (i == 0 && p.startsWith("/"))
                p = p.substring(1);

            if (p.isEmpty() || p.equals("."))
                continue;

            current = current.child(p);
        }
        return current;
    }

    default boolean notExists() {
        return !exists();
    }

    ByteBuffer map(FileChannel.MapMode mode) throws IOException;

    default boolean startsWith(String protectedPath) {
        if (protectedPath.startsWith("/")) {
            protectedPath = protectedPath.substring(1);
        }

        if (path().startsWith("/")) {
            return path().substring(1).startsWith(protectedPath + "/");
        } else {
            return path().startsWith(protectedPath + "/");
        }
    }
}
