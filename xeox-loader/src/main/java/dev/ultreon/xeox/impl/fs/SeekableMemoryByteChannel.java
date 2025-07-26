package dev.ultreon.xeox.impl.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class SeekableMemoryByteChannel implements SeekableByteChannel {
    private final ByteBuffer buffer;
    private final boolean readOnly;

    public SeekableMemoryByteChannel(byte[] bytes, boolean readOnly) {
        this.buffer = ByteBuffer.wrap(bytes);
        this.readOnly = readOnly;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int position = buffer.position();
        dst.put(buffer);
        return buffer.position() - position;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (readOnly) {
            throw new IOException("Cannot write to read-only channel");
        }
        int position = buffer.position();
        buffer.put(src);
        return buffer.position() - position;
    }

    @Override
    public long position() throws IOException {
        return buffer.position() + 1;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (newPosition < 0) {
            throw new IllegalArgumentException("Position cannot be negative");
        }
        if (newPosition > buffer.limit()) {
            throw new IllegalArgumentException("Position cannot be greater than buffer limit");
        }
        buffer.position((int) newPosition - 1);
        return this;
    }

    @Override
    public long size() throws IOException {
        return buffer.limit();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (readOnly) {
            throw new IOException("Cannot truncate read-only channel");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        if (size > buffer.limit()) {
            throw new IllegalArgumentException("Size cannot be greater than buffer limit");
        }
        buffer.limit((int) size);
        return this;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do here
    }
}
