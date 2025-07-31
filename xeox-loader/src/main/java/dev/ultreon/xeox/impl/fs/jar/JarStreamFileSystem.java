package dev.ultreon.xeox.impl.fs.jar;

import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;
import dev.ultreon.xeox.impl.fs.SeekableMemoryByteChannel;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarStreamFileSystem implements IFileSystem {
    private final JarInputStream inputStream;
    private final Map<String, byte[]> entries = new HashMap<>();

    public JarStreamFileSystem(JarInputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    SeekableByteChannel channelFor(String path) throws IOException {
        if (entries.isEmpty() || !entries.containsKey(path)) {
            return new SeekableMemoryByteChannel(readZip(path), true);
        }
        
        return new SeekableMemoryByteChannel(entries.get(path), true);
    }
    
    InputStream streamFor(String path) throws IOException {
        if (entries.isEmpty() || !entries.containsKey(path)) {
            return new ByteArrayInputStream(readZip(path));
        }
        
        return new ByteArrayInputStream(entries.get(path));
    }

    private byte[] readZip(String path) throws IOException {
        JarEntry nextJarEntry = inputStream.getNextJarEntry();
        while (nextJarEntry != null) {
            long size = nextJarEntry.getSize();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("File is too large to fit in memory");
            }

            byte[] buffer = new byte[1024];
            byte[] value = new byte[(int) size];
            int read;
            int index = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                if (index + read > value.length) throw new IOException("Size mismatch in zip file: " + path + " (" + index + " + " + read + " > " + value.length + ")");

                System.arraycopy(buffer, 0, value, index, read);
                index += read;

                if (index == value.length) break;
                if (inputStream.available() <= 0) break;
            }
            entries.put(path, value);
            nextJarEntry = inputStream.getNextJarEntry();
        }

        byte[] bytes = entries.get(path);
        if (bytes == null) {
            throw new FileNotFoundException(path);
        }
        return bytes;
    }

    @Override
    public IPath root() {
        return new JarStreamPath(this, "");
    }

    @Override
    public IPath path(String path) {
        if (path.startsWith("/")) {
            return new JarStreamPath(this, path);
        } else {
            return new JarStreamPath(this, "/" + path);
        }
    }

    @Override
    public IPath path(String first, String... more) {
        if (more.length == 0) {
            return path(first);
        } else {
            String[] paths = new String[more.length + 1];
            paths[0] = first;
            System.arraycopy(more, 0, paths, 1, more.length);
            return path(String.join("/", paths));
        }
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
