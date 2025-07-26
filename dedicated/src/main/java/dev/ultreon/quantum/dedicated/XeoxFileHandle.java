package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.xeox.api.IPath;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class XeoxFileHandle extends FileHandle {
    private final IPath path;

    public XeoxFileHandle(IPath path) {
        this.path = path;
    }

    @Override
    public String path() {
        return this.path.path();
    }

    @Override
    public String name() {
        String path1 = this.path.path();
        return path1.substring(path1.lastIndexOf('/') + 1);
    }

    @Override
    public String extension() {
        String path1 = this.path.path();
        int lastIndex = path1.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        } else {
            return path1.substring(lastIndex + 1);
        }
    }

    @Override
    public String nameWithoutExtension() {
        String path1 = this.name();
        int lastIndex = path1.lastIndexOf('.');
        if (lastIndex == -1) {
            return path1;
        } else {
            return path1.substring(0, lastIndex);
        }
    }

    @Override
    public String pathWithoutExtension() {
        String path1 = this.path.path();
        int lastIndex = path1.lastIndexOf('.');
        if (lastIndex == -1) {
            return path1;
        } else {
            return path1.substring(0, lastIndex);
        }
    }

    @Override
    public Files.FileType type() {
        return Files.FileType.Internal;
    }

    @Override
    public File file() {
        throw new UnsupportedOperationException("File is not supported!");
    }

    @Override
    public InputStream read() {
        try {
            return path.read();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public BufferedInputStream read(int bufferSize) {
        return new BufferedInputStream(this.read(), bufferSize);
    }

    @Override
    public Reader reader() {
        return new InputStreamReader(this.read());
    }

    @Override
    public Reader reader(String charset) {
        try {
            return new InputStreamReader(this.read(), charset);
        } catch (UnsupportedEncodingException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public BufferedReader reader(int bufferSize) {
        return new BufferedReader(new InputStreamReader(this.read()), bufferSize);
    }

    @Override
    public BufferedReader reader(int bufferSize, String charset) {
        try {
            return new BufferedReader(new InputStreamReader(this.read(), charset), bufferSize);
        } catch (UnsupportedEncodingException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public String readString() {
        try {
            return path.readString();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public String readString(String charset) {
        try (InputStream inputStream = this.read()) {
            byte[] buffer = new byte[1024];
            int length;
            StringBuilder stringBuilder = new StringBuilder();
            while ((length = inputStream.read(buffer)) > 0) {
                stringBuilder.append(new String(buffer, 0, length, charset));
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public byte[] readBytes() {
        try {
            return path.readBytes();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public int readBytes(byte[] bytes, int offset, int size) {
        try (InputStream inputStream = this.read()) {
            return inputStream.read(bytes, offset, size);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) {
        try {
            return path.map(mode);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public OutputStream write(boolean append) {
        try {
            return path.write(append);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public OutputStream write(boolean append, int bufferSize) {
        try {
            return path.write(append);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public void write(InputStream input, boolean append) {
        try (OutputStream outputStream = path.write(append)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public Writer writer(boolean append) {
        try (OutputStream outputStream = path.write(append)) {
            return new OutputStreamWriter(outputStream);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public Writer writer(boolean append, String charset) {
        try (OutputStream outputStream = path.write(append)) {
            return new OutputStreamWriter(outputStream, charset);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public void writeString(String string, boolean append) {
        try (OutputStream outputStream = path.write(append)) {
            outputStream.write(string.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public void writeString(String string, boolean append, String charset) {
        try (OutputStream outputStream = path.write(append)) {
            outputStream.write(string.getBytes(charset));
            outputStream.flush();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public void writeBytes(byte[] bytes, boolean append) {
        try (OutputStream outputStream = path.write(append)) {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
        try (OutputStream outputStream = path.write(append)) {
            outputStream.write(bytes, offset, length);
            outputStream.flush();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public FileHandle[] list() {
        try {
            List<FileHandle> handles = new ArrayList<>();
            for (String path1 : this.path.list()) {
                handles.add(new XeoxFileHandle(this.path.child(path1)));
            }
            return handles.toArray(new FileHandle[0]);
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public FileHandle[] list(FileFilter filter) {
        throw new UnsupportedOperationException("FileFilter is not supported!");
    }

    @Override
    public FileHandle[] list(FilenameFilter filter) {
        throw new UnsupportedOperationException("FilenameFilter is not supported!");
    }

    @Override
    public FileHandle[] list(String suffix) {
        List<FileHandle> handles = new ArrayList<>();
        for (FileHandle handle : this.list()) {
            if (handle.extension().equals(suffix)) {
                handles.add(handle);
            }
        }
        return handles.toArray(new FileHandle[0]);
    }

    public boolean isDirectory() {
        return path.isDirectory();
    }

    @Override
    public FileHandle child(String name) {
        return new XeoxFileHandle(path.child(name));
    }

    @Override
    public FileHandle sibling(String name) {
        return new XeoxFileHandle(path.sibling(name));
    }

    @Override
    public FileHandle parent() {
        return new XeoxFileHandle(path.parent());
    }

    @Override
    public void mkdirs() {
        if (isDirectory()) return;
        try {
            path.createDirectories();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public boolean exists() {
        return path.exists();
    }

    @Override
    public boolean delete() {
        if (path.isFile()) {
            try {
                path.delete();
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteDirectory() {
        if (isDirectory()) {
            try {
                path.delete();
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void emptyDirectory(boolean preserveTree) {
        if (preserveTree) {
            List<IOException> exceptions = new ArrayList<>();
            path.forEachRecursive(path1 -> {
                if (!path1.isDirectory()) {
                    try {
                        path1.delete();
                    } catch (IOException ignored) {
                    }
                }
            }, exceptions::add);
            if (!exceptions.isEmpty()) {
                GdxRuntimeException gdxRuntimeException = new GdxRuntimeException("Failed to empty directory: " + path + " with " + exceptions.size() + " exceptions");
                for (IOException exception : exceptions) {
                    gdxRuntimeException.addSuppressed(exception);
                }
                throw gdxRuntimeException;
            }
        }

        List<IOException> exceptions = new ArrayList<>();
        path.deleteRecursive(exceptions::add);
        if (!exceptions.isEmpty()) {
            GdxRuntimeException gdxRuntimeException = new GdxRuntimeException("Failed to empty directory: " + path + " with " + exceptions.size() + " exceptions");
            for (IOException exception : exceptions) {
                gdxRuntimeException.addSuppressed(exception);
            }
            throw gdxRuntimeException;
        }
    }

    @Override
    public void copyTo(FileHandle dest) {
        if (dest instanceof XeoxFileHandle) {
            path.copy(((XeoxFileHandle) dest).path, throwable -> {
                throw new GdxRuntimeException(throwable);
            });
            return;
        }

        try (InputStream in = this.read(); OutputStream out = dest.write(false)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
    }

    @Override
    public void moveTo(FileHandle dest) {
        if (dest instanceof XeoxFileHandle) {
            path.move(((XeoxFileHandle) dest).path, throwable -> {
                throw new GdxRuntimeException(throwable);
            });
            return;
        }

        try (InputStream in = this.read(); OutputStream out = dest.write(false)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }

        this.delete();
    }

    @Override
    public long length() {
        try {
            return path.length();
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public long lastModified() {
        try {
            return path.lastModified();
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != XeoxFileHandle.class) return false;
        XeoxFileHandle otherHandle = (XeoxFileHandle) obj;
        return path.path().equals(otherHandle.path.path()) && path.getClass() == otherHandle.path.getClass();
    }

    @Override
    public int hashCode() {
        return path.path().hashCode();
    }

    @Override
    public String toString() {
        return path.path();
    }
}
