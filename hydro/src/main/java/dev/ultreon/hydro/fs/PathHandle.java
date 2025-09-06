package dev.ultreon.hydro.fs;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface PathHandle {
    String path();

    String name();

    String extension();

    String nameWithoutExtension();

    boolean exists();

    boolean isDirectory();

    boolean isFile();

    boolean create();

    boolean delete();

    boolean rename(String newName);

    boolean move(String newPath);

    boolean move(PathHandle newPath);

    String[] listNames();

    PathHandle[] listFiles();

    @Nullable PathHandle getParent();

    long lastModified();

    long length();

    boolean isReadable();

    boolean isWritable();

    boolean isExecutable();

    PathHandle resolve(String data);

    InputStream read() throws Exception;

    void write(InputStream data) throws Exception;

    void write(byte[] data) throws Exception;

    void write(byte[] data, int offset, int length) throws Exception;

    void write(String data) throws Exception;

    void write(String data, int offset, int length) throws Exception;

    byte[] readAllBytes() throws Exception;

    String readAllText() throws Exception;

    String readAllText(String encoding) throws Exception;

    void copy(PathHandle destination) throws Exception;

    void copy(String destination) throws Exception;
}
