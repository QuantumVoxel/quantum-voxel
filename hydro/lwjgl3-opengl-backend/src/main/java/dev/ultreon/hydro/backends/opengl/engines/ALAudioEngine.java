package dev.ultreon.hydro.backends.opengl.engines;

import dev.ultreon.hydro.audio.Sound;
import dev.ultreon.hydro.backends.opengl.audio.ALSound;
import dev.ultreon.hydro.backends.opengl.audio.AudioData;
import dev.ultreon.hydro.engine.AudioEngine;
import dev.ultreon.hydro.fs.PathHandle;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;

public class ALAudioEngine implements AudioEngine {
    private final long device;
    private final long context;
    private final ALCCapabilities alcCaps;
    private final ALCapabilities alCaps;

    public ALAudioEngine() {
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        context = ALC10.alcCreateContext(device, (int[]) null);
        alcCaps = ALC.createCapabilities(context);
        alCaps = AL.createCapabilities(alcCaps);
    }

    @Override
    public Sound createSound(PathHandle path) {
        return new ALSound(decodeAudio(path));
    }

    private AudioData decodeAudio(PathHandle path) {

    }
}
