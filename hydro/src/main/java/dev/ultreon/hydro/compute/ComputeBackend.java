package dev.ultreon.hydro.compute;

public enum ComputeBackend {
    OpenCL,
    Vulkan,
    Metal,
    WebGPU,
    Glsl,
    Unknown,
}
