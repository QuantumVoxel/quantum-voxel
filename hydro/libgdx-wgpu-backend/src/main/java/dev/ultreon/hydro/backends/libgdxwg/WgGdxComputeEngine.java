package dev.ultreon.hydro.backends.libgdxwg;

import dev.ultreon.hydro.compute.ComputeBackend;
import dev.ultreon.hydro.compute.ComputeProgram;
import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.engine.ComputeEngine;

public class WgGdxComputeEngine implements ComputeEngine {
    public WgGdxComputeEngine(Application app) {

    }

    @Override
    public ComputeBackend getBackend() {
        return ComputeBackend.WebGPU;
    }

    @Override
    public ComputeProgram createProgram(String source) {
        throw new UnsupportedOperationException("WebGPU does not support creating compute programs");
    }
}
