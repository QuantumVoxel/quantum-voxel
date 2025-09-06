package dev.ultreon.hydro.engine;

import dev.ultreon.hydro.compute.ComputeBackend;
import dev.ultreon.hydro.compute.ComputeProgram;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ComputeEngine {
    ComputeBackend getBackend();

    ComputeProgram createProgram(String source);
}
