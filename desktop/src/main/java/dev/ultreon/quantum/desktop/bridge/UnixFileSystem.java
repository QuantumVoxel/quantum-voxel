package dev.ultreon.quantum.desktop.bridge;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public class UnixFileSystem extends FileSystem {
    private static UnixFileSystem defaultFileSystem;
    private final URI uri;
    private final Map<String, ?> env;
    private List<Path> paths;
    private Iterable<FileStore> fileStores = new ArrayList<>();

    public UnixFileSystem(URI uri, Map<String, ?> env) {
        this.uri = uri;
        this.env = env;
        paths = Arrays.asList(new UnixPath("/", this));
        fileStores = Arrays.asList(new UnixFileStore(this));
    }

    public static UnixFileSystem getDefault() {
        if (defaultFileSystem == null) {
            defaultFileSystem = new UnixFileSystem(URI.create("unix://"), Map.of());
        }
        return defaultFileSystem;
    }

    @Override
    public FileSystemProvider provider() {
        return UnixFileSystemProvider.INSTANCE;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        paths = Arrays.asList(new UnixPath("/", this));
        return paths;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return fileStores;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Paths.get("/").getFileSystem().supportedFileAttributeViews();
    }

    @Override
    public @NotNull Path getPath(@NotNull String first, @NotNull String... more) {
        return UnixPath.getPath(first, more, this);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("Path matcher not supported");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new IOException("Watch service not supported");
    }
}
