package dev.ultreon.hydro.backends.libgdx;

import com.badlogic.gdx.Gdx;
import dev.ultreon.hydro.audio.Sound;
import dev.ultreon.hydro.backends.libgdx.fs.GdxPathHandle;
import dev.ultreon.hydro.backends.libgdx.fs.GdxSound;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.engine.AudioEngine;
import dev.ultreon.hydro.fs.PathHandle;

public class LibGDXAudioEngine implements AudioEngine {
    public LibGDXAudioEngine(Application app) {

    }

    @Override
    public Sound createSound(PathHandle path) {
        return new GdxSound(Gdx.audio.newSound(((GdxPathHandle) path).toGdx()));
    }
}
