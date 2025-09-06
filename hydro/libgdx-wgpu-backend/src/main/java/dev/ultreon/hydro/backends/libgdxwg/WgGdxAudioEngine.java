package dev.ultreon.hydro.backends.libgdxwg;

import com.badlogic.gdx.Gdx;
import dev.ultreon.hydro.audio.Sound;
import dev.ultreon.hydro.backends.libgdxwg.fs.WgGdxPathHandle;
import dev.ultreon.hydro.backends.libgdxwg.fs.WgGdxSound;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.engine.AudioEngine;
import dev.ultreon.hydro.fs.PathHandle;

public class WgGdxAudioEngine implements AudioEngine {
    public WgGdxAudioEngine(Application app) {

    }

    @Override
    public Sound createSound(PathHandle path) {
        return new WgGdxSound(Gdx.audio.newSound(((WgGdxPathHandle) path).toGdx()));
    }
}
