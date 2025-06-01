package dev.ultreon.quantum.client.resources;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A file handle for resources.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ResourceFileHandle extends FileHandle {
    private final NamespaceID id;
    private final @Nullable Resource resource;
    private final List<NamespaceID> subResources;

    /**
     * Constructs a new ResourceFileHandle with the given namespace ID.
     * 
     * @param id The namespace ID of the resource.
     */
    public ResourceFileHandle(NamespaceID id) {
        super(id.toString());
        this.id = id;
        this.resource = QuantumClient.get().getResourceManager().getResource(id);

        if (this.resource != null) {
            this.subResources = List.of();
        } else {
            this.subResources = QuantumClient.get().getResourceManager().getResourcePackages().stream()
                    .flatMap(it -> it.entries().stream())
                    .filter(it -> it.getPath().startsWith(id.getPath()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Constructs a new ResourceFileHandle with the given resource.
     * 
     * @param resource The resource.
     */
    public ResourceFileHandle(@NotNull Resource resource) {
        super("generated_" + UUID.randomUUID().toString().replace("-", ""));

        this.id = new NamespaceID("java", "generated_" + UUID.randomUUID().toString().replace("-", ""));
        this.resource = resource;

        this.subResources = List.of();
    }

    /**
     * Gets the namespace ID of the resource.
     * 
     * @return The namespace ID of the resource.
     */
    public NamespaceID getId() {
        return this.id;
    }

    /**
     * Gets the resource.
     * 
     * @return The resource.
     */
    public @Nullable Resource getResource() {
        return this.resource;
    }

    @Override
    public ResourceFileHandle parent() {
        String path = id.getPath();
        int endIndex = path.lastIndexOf('/');
        if (endIndex == -1) return new ResourceFileHandle(id.withPath(""));
        String parent = path.substring(0, endIndex);
        return new ResourceFileHandle(id.withPath(parent));
    }

    @Override
    public ResourceFileHandle sibling(String name) {
        String path = id.getPath();
        int endIndex = path.lastIndexOf('/');
        if (endIndex == -1) return new ResourceFileHandle(id.withPath(name));
        String parent = path.substring(0, endIndex);
        return new ResourceFileHandle(id.withPath(parent + "/" + name));
    }

    @Override
    public ResourceFileHandle child(String name) {
        return new ResourceFileHandle(id.withPath(id.getPath() + "/" + name));
    }

    @Override
    public File file() {
        throw new GdxRuntimeException("Cannot get file from a ResourceFileHandle");
    }

    @Override
    public boolean isDirectory() {
        if (!exists()) return false;
        return resource == null && !subResources.isEmpty();
    }

    @Override
    public FileHandle[] list() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        return subResources.stream()
                .filter(it -> it.getDomain().equals(this.id.getDomain())
                              && it.getPath().lastIndexOf('/') == this.id.getPath().length())
                .map(ResourceFileHandle::new)
                .toArray(FileHandle[]::new);
    }

    @Override
    public FileHandle[] list(String suffix) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        return subResources.stream()
                .filter(it -> it.getDomain().equals(this.id.getDomain())
                              && it.getPath().lastIndexOf('/') == this.id.getPath().length()
                              && it.getPath().endsWith(suffix))
                .map(ResourceFileHandle::new)
                .toArray(FileHandle[]::new);
    }

    @Override
    public FileHandle[] list(FileFilter filter) {
        throw new GdxRuntimeException("Cannot list files using a FileFilter from a ResourceFileHandle");
    }

    @Override
    public FileHandle[] list(FilenameFilter filter) {
        throw new GdxRuntimeException("Cannot list files using a FilenameFilter from a ResourceFileHandle");
    }

    @Override
    public String readString() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        byte[] bytes = this.resource.loadOrGet();
        if (bytes == null) throw new GdxRuntimeException(String.format("Resource %s failed to load", this.id));
        return new String(bytes);
    }

    @Override
    public byte[] readBytes() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        byte[] bytes = this.resource.loadOrGet();
        if (bytes == null) throw new GdxRuntimeException(String.format("Resource %s failed to load", this.id));
        return bytes.clone();
    }

    @Override
    public BufferedInputStream read(int bufferSize) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            return new BufferedInputStream(this.resource.openStream(), bufferSize);
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while reading resource %s", this.id), e);
        }
    }

    @Override
    public long length() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        byte[] bytes = this.resource.loadOrGet();
        if (bytes == null) throw new GdxRuntimeException(String.format("Resource %s failed to load", this.id));
        return bytes.length;
    }

    @Override
    public long lastModified() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        return 0;
    }

    @Override
    public boolean delete() {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public boolean deleteDirectory() {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void emptyDirectory() {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void emptyDirectory(boolean preserveTree) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void mkdirs() {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public String name() {
        return this.id.getPath().substring(this.id.getPath().lastIndexOf('/') + 1);
    }

    @Override
    public String path() {
        return this.id.toString();
    }

    @Override
    public String nameWithoutExtension() {
        String name = name();
        return name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    public String extension() {
        String name = name();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public Reader reader() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            return this.resource.openReader();
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while reading resource %s", this.id), e);
        }
    }

    @Override
    public BufferedReader reader(int bufferSize) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            return new BufferedReader(this.resource.openReader(), bufferSize);
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while reading resource %s", this.id), e);
        }
    }

    @Override
    public BufferedReader reader(int bufferSize, String charset) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            return new BufferedReader(this.resource.openReader(), bufferSize);
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while reading resource %s", this.id), e);
        }
    }

    @Override
    public String readString(String charset) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        byte[] bytes = this.resource.loadOrGet();
        if (bytes == null) throw new GdxRuntimeException(String.format("Resource %s failed to load", this.id));
        try {
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            throw new GdxRuntimeException(String.format("Unsupported encoding %s while reading resource %s", charset, this.id), e);
        }
    }

    @Override
    public OutputStream write(boolean append) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public OutputStream write(boolean append, int bufferSize) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void write(InputStream input, boolean append) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public Writer writer(boolean append) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public Writer writer(boolean append, String charset) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void writeString(String string, boolean append) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void writeString(String string, boolean append, String charset) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void writeBytes(byte[] bytes, boolean append) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public Reader reader(String charset) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            return this.resource.openReader();
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while reading resource %s", this.id), e);
        }
    }

    @Override
    public int readBytes(byte[] bytes, int offset, int size) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        byte[] data = this.resource.loadOrGet();
        if (data == null) throw new GdxRuntimeException(String.format("Resource %s failed to load", this.id));
        if (offset + size > bytes.length) {
            System.arraycopy(data, 0, bytes, offset, bytes.length - offset);
            return bytes.length - offset;
        }
        System.arraycopy(data, 0, bytes, offset, size);
        return size;
    }

    @Override
    public String pathWithoutExtension() {
        return this.id.toString().substring(0, this.id.toString().lastIndexOf('.'));
    }

    @Override
    public Files.FileType type() {
        return Files.FileType.Internal;
    }

    @Override
    public ByteBuffer map() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        byte[] data = this.resource.loadOrGet();
        if (data == null) throw new GdxRuntimeException(String.format("Resource %s failed to load", this.id));
        return ByteBuffer.wrap(data.clone());
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        if (mode != FileChannel.MapMode.READ_ONLY) throw new GdxRuntimeException("Only read-only mode is supported");
        byte[] data = this.resource.loadOrGet();
        if (data == null) throw new GdxRuntimeException(String.format("Resource %s failed to load", this.id));
        return ByteBuffer.wrap(data.clone());
    }

    @Override
    public void copyTo(FileHandle dest) {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            byte[] buffer = new byte[1024];
            int read;
            try (InputStream in = this.resource.openStream();
                 OutputStream out = dest.write(false)) {
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while copying resource %s to %s", this.id, dest), e);
        }
    }

    @Override
    public void moveTo(FileHandle dest) {
        throw new GdxRuntimeException("Read-only file handle");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResourceFileHandle that = (ResourceFileHandle) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getResource(), that.getResource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), getResource());
    }

    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Reads the resource.
     * 
     * @return The resource.
     */
    @Override
    public InputStream read() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            return this.resource.openStream();
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while reading resource %s", this.id), e);
        }
    }

    /**
     * Checks if the resource exists.
     * 
     * @return True if the resource exists, false otherwise.
     */
    @Override
    public boolean exists() {
        return this.resource != null || !this.subResources.isEmpty();
    }
}
