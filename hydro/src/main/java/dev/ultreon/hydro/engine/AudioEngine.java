package dev.ultreon.hydro.engine;

import dev.ultreon.hydro.audio.Sound;
import dev.ultreon.hydro.fs.PathHandle;

public interface AudioEngine {
    Sound createSound(PathHandle path);
}
