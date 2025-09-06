package dev.ultreon.hydro.backends.libgdxwg.fs;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.hydro.fs.PathHandle;
import dev.ultreon.hydro.fs.StorageType;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;

public class WgGdxPathHandle implements PathHandle {
    private final Files.FileType type;
    private final FileHandle handle;

    public WgGdxPathHandle(String newPath, Files.FileType type) {
        this.type = type;
        this.handle = Gdx.files.getFileHandle(newPath, type);
    }

    public WgGdxPathHandle(String newPath, StorageType type) {
        this(newPath, toFileType(type));
    }

    public WgGdxPathHandle(FileHandle child) {
        this.type = child.type();
        this.handle = child;
    }

    private static WgGdxPathHandle get(String newPath, Files.FileType type) {
        return new WgGdxPathHandle(newPath, type);
    }

    private static Files.FileType toFileType(StorageType type) {
        switch (type) {
            case ASSETS:
                return Files.FileType.Internal;
            case LOCAL:
                return Files.FileType.Local;
            case EXTERNAL:
                return Files.FileType.External;
            default:
                return Files.FileType.Absolute;
        }
    }

    @Override
    public boolean isFile() {
        return !isDirectory();
    }

    @Override
    public String path() {
        return handle.path();
    }

    @Override
    public String name() {
        return handle.name();
    }

    @Override
    public String extension() {
        return handle.extension();
    }

    @Override
    public String nameWithoutExtension() {
        return handle.nameWithoutExtension();
    }

    @Override
    public boolean exists() {
        return handle.exists();
    }

    @Override
    public boolean isDirectory() {
        return handle.isDirectory();
    }

    @Override
    public boolean create() {
        try (OutputStream os = handle.write(false)) {
            return os != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delete() {
        return handle.delete();
    }

    @Override
    public boolean rename(String newName) {
        try {
            handle.moveTo(handle.sibling(newName));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean move(String newPath) {
        try {
            handle.moveTo(Gdx.files.getFileHandle(newPath, type));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean move(PathHandle newPath) {
        try {
            handle.moveTo(((WgGdxPathHandle) newPath).handle);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String[] listNames() {
        FileHandle[] list = handle.list();
        String[] names = new String[list.length];
        for (int i = 0; i < list.length; i++) {
            names[i] = list[i].name();
        }
        return names;
    }

    @Override
    public PathHandle[] listFiles() {
        FileHandle[] list = handle.list();
        PathHandle[] handles = new PathHandle[list.length];
        for (int i = 0; i < list.length; i++) {
            handles[i] = new WgGdxPathHandle(list[i]);
        }
        return handles;
    }

    @Override
    public @Nullable PathHandle getParent() {
        return new WgGdxPathHandle(handle.parent());
    }

    @Override
    public long lastModified() {
        return handle.lastModified();
    }

    @Override
    public long length() {
        return handle.length();
    }

    @Override
    public boolean isReadable() {
        try {
            return exists();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isWritable() {
        return type != Files.FileType.Classpath && type != Files.FileType.Internal;
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public PathHandle resolve(String data) {
        return new WgGdxPathHandle(handle.child(data));
    }

    @Override
    public InputStream read() {
        return handle.read();
    }

    @Override
    public void write(InputStream data) {
        handle.write(data, false);
    }

    @Override
    public void write(byte[] data) {
        handle.writeBytes(data, false);
    }

    @Override
    public void write(byte[] data, int offset, int length) {
        handle.writeBytes(data, offset, length, false);
    }

    @Override
    public void write(String data) {
        handle.writeString(data, false);
    }

    @Override
    public void write(String data, int offset, int length) {
        handle.writeString(data.substring(offset, offset + length), false);
    }

    @Override
    public byte[] readAllBytes() {
        return handle.readBytes();
    }

    @Override
    public String readAllText() {
        return handle.readString();
    }

    @Override
    public String readAllText(String encoding) {
        return handle.readString(encoding);
    }

    @Override
    public void copy(PathHandle destination) {
        ((WgGdxPathHandle) destination).handle.writeBytes(handle.readBytes(), false);
    }

    @Override
    public void copy(String destination) {
        ((WgGdxPathHandle) resolve(destination)).handle.writeBytes(handle.readBytes(), false);
    }

    public FileHandle toGdx() {
        return handle;
    }
}
