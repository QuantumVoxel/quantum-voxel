package dev.ultreon.hydro.compute;

import dev.ultreon.hydro.core.Application;
import dev.ultreon.hydro.engine.ComputeEngine;

public class MockComputeEngine implements ComputeEngine {
    public MockComputeEngine(Application app) {

    }

    @Override
    public ComputeBackend getBackend() {
        return ComputeBackend.Unknown;
    }

    @Override
    public ComputeProgram createProgram(String source) {
        throw new UnsupportedOperationException("Mock Engine does not support creating compute programs");
    }
}
