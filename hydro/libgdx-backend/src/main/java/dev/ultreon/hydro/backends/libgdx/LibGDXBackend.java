package dev.ultreon.hydro.backends.libgdx;

import dev.ultreon.hydro.Hydro;
import dev.ultreon.hydro.compute.MockComputeEngine;
import dev.ultreon.hydro.core.Application;

public class LibGDXBackend {
    public LibGDXBackend(Application app) {
        Hydro.graphics = new LibGDXGraphicsEngine(app);
        Hydro.input = new LibGDXInputEngine(app);
        Hydro.audio = new LibGDXAudioEngine(app);
        Hydro.compute = new MockComputeEngine(app);
        Hydro.io = new LibGDXIOEngine(app);
    }
}
