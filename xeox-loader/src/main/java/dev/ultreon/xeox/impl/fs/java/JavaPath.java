package dev.ultreon.xeox.impl.fs.java;

import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;
import dev.ultreon.xeox.impl.fs.isolated.IsolatedFileSystem;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class JavaPath implements IPath {
    private final IFileSystem fs;
    private final Path path;

    public JavaPath(IFileSystem fs, Path path) {
        this.fs = fs;
        this.path = path;
    }

    @Override
    public String path() {
        return path.toString();
    }

    @Override
    public IPath parent() {
        return fs.path(path.getParent().toString());
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
                }
                current = current.child(part);
            }
            return current;
        }
        
        if (name.startsWith("/")) {
            return fs.path(path.toString(), name.substring(1));
        } else {
            return fs.path(path.toString(), name);
        }
    }

    @Override
    public IPath child(String first, String... more) {
        IPath current = this.child(first);
        for (String part : more) {
            current = current.child(part);
        }
        return current;
    }

    @Override
    public IPath sibling(String name) {
        return parent().child(name);
    }

    @Override
    public boolean exists() {
        if (fs.isProtected(path())) {
            return false;
        }

        return Files.exists(path);
    }

    @Override
    public boolean isDirectory() {
        if (fs.isProtected(path())) {
            return false;
        }

        return Files.isDirectory(path);
    }

    @Override
    public boolean isFile() {
        if (fs.isProtected(path())) {
            return false;
        }

        return Files.isRegularFile(path);
    }

    @Override
    public boolean create() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        try {
            Files.createDirectories(path.getParent());
            return Files.exists(Files.createFile(path));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean createDirectory() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.exists(Files.createDirectories(path));
    }

    @Override
    public boolean delete() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.deleteIfExists(path);
    }

    @Override
    public boolean rename(String newName) throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.exists(Files.move(path, path.getParent().resolve(newName)));
    }

    @Override
    public void move(IPath destination, Consumer<Throwable> onError) {
        if (fs.isProtected(path())) {
            onError.accept(new IOException("Cannot stat protected file"));
            return;
        }
        if (destination.exists()) {
            return;
        }

        IFileSystem fs = new IsolatedFileSystem(destination, false);
        if (isDirectory()) {
            forEachRecursive(path1 -> {
                if (path1.isDirectory()) {
                    fs.path(path1.path()).createDirectories();
                    return;
                }

                try (InputStream in = path1.read(); OutputStream out = fs.path(path1.path()).write(false)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    onError.accept(e);
                }
            }, onError::accept);
        }

    }

    @Override
    public void copy(IPath destination, Consumer<Throwable> onError) {
        if (fs.isProtected(path())) {
            onError.accept(new IOException("Cannot stat protected file"));
            return;
        }

        if (destination.exists()) {
            return;
        }

        IFileSystem fs = new IsolatedFileSystem(destination, false);
        if (isDirectory()) {
            forEachRecursive(path1 -> {
                if (path1.isDirectory()) {
                    fs.path(path1.path()).createDirectories();
                    return;
                }

                try (InputStream in = path1.read(); OutputStream out = fs.path(path1.path()).write(false)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    onError.accept(e);
                }
            }, onError::accept);
        }

    }

    @Override
    public String[] list() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        try (Stream<Path> list = Files.list(path)) {
            return list.map(path1 -> path1.getFileName().toString()).toArray(String[]::new);
        }
    }

    @Override
    public String[] list(String extension) throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        try (Stream<Path> list = Files.list(path)) {
            return list.filter(path1 -> path1.getFileName().toString().endsWith(extension)).map(path1 -> path1.getFileName().toString()).toArray(String[]::new);
        }
    }

    @Override
    public long lastModified() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.getLastModifiedTime(path).toMillis();
    }

    @Override
    public long length() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.size(path);
    }

    @Override
    public boolean isWritable() {
        if (fs.isProtected(path())) {
            return false;
        }

        return Files.isWritable(path);
    }

    @Override
    public boolean isReadable() {
        if (fs.isProtected(path())) {
            return false;
        }

        return Files.isReadable(path);
    }

    @Override
    public InputStream read() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.newInputStream(path);
    }

    @Override
    public OutputStream write(boolean append) throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.newOutputStream(path);
    }

    @Override
    public ByteChannel channel() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.newByteChannel(path);
    }

    @Override
    public @Nullable SeekableByteChannel seekableChannel() throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        return Files.newByteChannel(path);
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) throws IOException {
        if (fs.isProtected(path())) {
            throw new IOException("Cannot stat protected file");
        }

        try (FileChannel channel = FileChannel.open(path)) {
            return channel.map(mode, 0, channel.size());
        }
    }

    @Override
    public String toString() {
        return path();
    }
}
