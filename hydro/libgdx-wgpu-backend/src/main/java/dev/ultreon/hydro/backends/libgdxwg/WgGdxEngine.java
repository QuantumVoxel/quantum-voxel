package dev.ultreon.hydro.backends.libgdxwg;

import dev.ultreon.hydro.Hydro;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.Engine;

public class WgGdxEngine implements Engine {
    public WgGdxEngine(Application app) {
        Hydro.graphics = new WgGdxGraphicsEngine(app);
        Hydro.input = new WgGdxInputEngine(app);
        Hydro.audio = new WgGdxAudioEngine(app);
        Hydro.compute = new WgGdxComputeEngine(app);
        Hydro.io = new WgGdxIOEngine(app);
    }
}
