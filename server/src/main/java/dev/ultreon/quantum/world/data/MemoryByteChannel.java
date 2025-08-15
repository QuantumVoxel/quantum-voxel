package dev.ultreon.quantum.world.data;

import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class MemoryByteChannel implements SeekableByteChannel {
    private byte[] data = new byte[1024];
    private ByteBuffer buffer = ByteBuffer.wrap(data);
    private long position = 0;
    private long size = 0;

    @Override
    public int read(ByteBuffer dst) {
        int read = Math.min(dst.remaining(), (int) (size - position));
        if(read > 0) {
            dst.put(data, (int) position, read);
            position += read;
        }
        return read;
    }

    @Override
    public int write(ByteBuffer src) {
        // Resize if necessary
        if(size < position + src.remaining()) {
            byte[] newBuffer = new byte[(int) (position + src.remaining())];
            System.arraycopy(data, 0, newBuffer, 0, data.length);
            src.get(newBuffer, (int) position, src.remaining());
            data = newBuffer;
            buffer = ByteBuffer.wrap(data);
        }

        src.get(data, (int) position, src.remaining());
        position += src.remaining();
        size = Math.max(size, position);

        return src.remaining();
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) {
        position = newPosition;
        return this;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        if(size < this.size) {
            this.size = size;
            if(size < data.length) {
                byte[] newBuffer = new byte[(int) size];
                System.arraycopy(data, 0, newBuffer, 0, (int) size);
                data = newBuffer;
                buffer = ByteBuffer.wrap(data);
            }
        }

        return this;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    public void writeTo(FileHandle file) {
        file.writeBytes(data, 0, (int) size, false);
    }

    public void readFrom(FileHandle file) {
        data = file.readBytes();
        buffer = ByteBuffer.wrap(data);
    }

    public ByteBuffer buffer() {
        return buffer;
    }
}
