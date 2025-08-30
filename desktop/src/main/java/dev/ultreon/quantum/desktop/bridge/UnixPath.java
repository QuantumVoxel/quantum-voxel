package dev.ultreon.quantum.desktop.bridge;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;

public class UnixPath implements Path {
    private final @NotNull String[] path;
    private final boolean absolute;
    private @NotNull FileSystem fileSystem;

    public UnixPath(URI uri, @NotNull FileSystem fileSystem) {
        this(uri.getPath(), fileSystem);
    }

    public UnixPath(@NotNull String[] path, FileSystem fileSystem) {
        this.path = path;
        if (path.length == 0) {
            absolute = false;
        } else {
            absolute = path[0].equals("/");
        }
        this.fileSystem = fileSystem;
    }

    public UnixPath(boolean absolute, @NotNull String[] path, @NotNull FileSystem fileSystem) {
        this.absolute = absolute;
        this.path = path;
        this.fileSystem = fileSystem;
    }

    public UnixPath(String s, @NotNull FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        if (!s.startsWith("/")) {
            s = "/" + s;
        }
        s = s.replaceAll("//", "/");
        absolute = s.startsWith("/");
        if (s.startsWith("/")) s = s.substring(1);
        this.path = s.split("/");
        for (int i = 0; i < path.length; i++) {
            if (i == path.length - 1) continue;
            path[i] = path[i] + "/";
        }
    }

    public static @NotNull Path getPath(@NotNull String first, @NotNull String[] more, UnixFileSystem fileSystem) {
        boolean absolute = first.startsWith("/");
        if (first.equals("/")) {
            return new UnixPath(absolute, more, fileSystem);
        }
        first = absolute ? first.substring(1) : first;
        String[] path = new String[more.length + 1];
        path[0] = first;
        System.arraycopy(more, 0, path, 1, more.length);
        return new UnixPath(absolute, path, fileSystem);
    }

    @Override
    public @NotNull FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return path[0].equals("/");
    }

    @Override
    public Path getRoot() {
        return fileSystem.getRootDirectories().iterator().next();
    }

    @Override
    public Path getFileName() {
        return path.length > 0 ? new UnixPath(false, new String[]{path[path.length - 1]}, fileSystem) : null;
    }

    @Override
    public Path getParent() {
        if (path.length == 0) return null;
        return new UnixPath(false, new String[]{path[0]}, fileSystem);
    }

    @Override
    public int getNameCount() {
        return path.length;
    }

    @Override
    public @NotNull Path getName(int index) {
        return new UnixPath(false, new String[]{path[index]}, fileSystem);
    }

    @Override
    public @NotNull Path subpath(int beginIndex, int endIndex) {
        String[] subpath = new String[endIndex - beginIndex];
        System.arraycopy(path, beginIndex, subpath, 0, endIndex - beginIndex);
        return new UnixPath(false, subpath, fileSystem);
    }

    @Override
    public boolean startsWith(@NotNull Path other) {
        if (other.isAbsolute() == absolute) return false;
        if (other.getNameCount() > getNameCount()) return false;
        for (int i = 0; i < other.getNameCount(); i++) {
            if (!other.getName(i).equals(getName(i))) return false;
        }
        return true;
    }

    @Override
    public boolean endsWith(@NotNull Path other) {
        if (other.isAbsolute() == absolute) return false;
        if (other.getNameCount() > getNameCount()) return false;
        for (int i = 0; i < other.getNameCount(); i++) {
            if (!other.getName(other.getNameCount() - i - 1).equals(getName(getNameCount() - i - 1))) return false;
        }

        return true;
    }

    @Override
    public @NotNull Path normalize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Path resolve(@NotNull Path other) {
        return new UnixPath(absolute, new String[]{path[0]}, fileSystem);
    }

    @Override
    public @NotNull Path relativize(@NotNull Path other) {
        UnixPath otherPath = (UnixPath) other;
        if (otherPath.isAbsolute() == absolute) return other;
        String[] relPath = new String[getNameCount() - otherPath.getNameCount()];
        Arrays.fill(relPath, "..");
        System.arraycopy(otherPath.path, getNameCount() - otherPath.getNameCount(), relPath, relPath.length - otherPath.getNameCount(), otherPath.getNameCount() - otherPath.getNameCount());
        return new UnixPath(false, relPath, fileSystem);
    }

    @Override
    public @NotNull URI toUri() {
        try {
            return new URI("unix", null, (absolute ? "/" : "") + dev.ultreon.quantum.StringUtils.join("/", path), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Path toAbsolutePath() {
        return new UnixPath(true, path, fileSystem);
    }

    @Override
    public @NotNull Path toRealPath(@NotNull LinkOption... options) throws IOException {
        return toAbsolutePath();
    }

    @Override
    public @NotNull WatchKey register(@NotNull WatchService watcher, WatchEvent.@NotNull Kind<?>[] events, @NotNull WatchEvent.Modifier... modifiers) throws IOException {
        throw new IOException("Watch service not supported");
    }

    @Override
    public int compareTo(@NotNull Path other) {
        if (other.isAbsolute() && !absolute) return -1;
        if (!other.isAbsolute() && absolute) return 1;
        for (int i = 0; i < Math.min(getNameCount(), other.getNameCount()); i++) {
            int result = getName(i).compareTo(other.getName(i));
            if (result != 0) return result;
        }
        return Integer.compare(getNameCount(), other.getNameCount());
    }

    @Override
    public String toString() {
        if (absolute) return "/" + dev.ultreon.quantum.StringUtils.join("/", path);
        return dev.ultreon.quantum.StringUtils.join("/", path);
    }
}
