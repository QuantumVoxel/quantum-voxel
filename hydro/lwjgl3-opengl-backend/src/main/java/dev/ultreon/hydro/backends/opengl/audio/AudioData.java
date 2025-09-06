package dev.ultreon.hydro.backends.opengl.audio;

import dev.ultreon.hydro.core.Destroyable;
import dev.ultreon.hydro.core.HydroException;
import dev.ultreon.hydro.fs.PathHandle;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class AudioData {
    private final IntBuffer channels;
    private final IntBuffer sampleRate;

    public AudioData(PathHandle handle) {
        if (!handle.extension().equals(".ogg")) {
            throw new HydroException("Only OGG Vorbis is supported");
        }
        byte[] bytes;
        try {
            bytes = handle.readAllBytes();
        } catch (Exception e) {
            throw new HydroException("Failed to read OGG file", e);
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            channels = memoryStack.mallocInt(1);
            sampleRate = memoryStack.mallocInt(1);

            int[] error = new int[1];
            ByteBuffer alloc = memoryStack.bytes(bytes);
            long l = STBVorbis.stb_vorbis_open_memory(alloc, error, null);
            if (l == 0) {
                throw new HydroException("Decoding error when reading " + handle.path());
            }
//            STBVorbis.stb_vorbis_decode_frame_pushdata()
        }
    }

    public Buffer getData() {
        return null;
    }

    public int getChannels() {
        return channels.get(0);
    }

    public int getSampleRate() {
        return sampleRate.get(0);
    }
}
