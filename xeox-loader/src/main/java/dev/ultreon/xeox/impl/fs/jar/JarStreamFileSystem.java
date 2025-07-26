package dev.ultreon.xeox.impl.fs.jar;

import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;
import dev.ultreon.xeox.impl.fs.SeekableMemoryByteChannel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;
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
            return new SeekableMemoryByteChannel(locateEntry(path), true);
        }
        
        return new SeekableMemoryByteChannel(entries.get(path), true);
    }
    
    InputStream streamFor(String path) throws IOException {
        if (entries.isEmpty() || !entries.containsKey(path)) {
            return new ByteArrayInputStream(locateEntry(path));
        }
        
        return new ByteArrayInputStream(entries.get(path));
    }

    private byte[] locateEntry(String path) throws IOException {
        JarEntry nextJarEntry = inputStream.getNextJarEntry();
        while (nextJarEntry != null) {
            byte[] value = inputStream.readAllBytes();
            entries.put(path, value);
            if (path.equals(nextJarEntry.getName())) {
                return value;
            }
            nextJarEntry = inputStream.getNextJarEntry();
        }
        throw new IOException("Could not find entry " + path);
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
