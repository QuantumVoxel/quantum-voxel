package dev.ultreon.quantum.desktop.bridge;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;

public class UnixFileStore extends FileStore {
    public UnixFileStore(UnixFileSystem unixFileSystem) {

    }

    @Override
    public String name() {
        return "/";
    }

    @Override
    public String type() {
        return "root";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return FileSystems.getDefault().getPath("/").toFile().getTotalSpace();
    }

    @Override
    public long getUsableSpace() throws IOException {
        return FileSystems.getDefault().getPath("/").toFile().getUsableSpace();
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return FileSystems.getDefault().getPath("/").toFile().getFreeSpace();
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return type.equals(PosixFileAttributeView.class);
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        return name.equals("posix") || name.equals("unix");
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return FileSystems.getDefault().getFileStores().iterator().next().getFileStoreAttributeView(type);
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        return FileSystems.getDefault().getFileStores().iterator().next().getAttribute(attribute);
    }
}
