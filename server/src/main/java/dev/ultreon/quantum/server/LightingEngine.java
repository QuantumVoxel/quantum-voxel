package dev.ultreon.quantum.server;

public interface LightingEngine {
    boolean addLight(LightContainer world, int x, int y, int z, int light);

    boolean removeLight(LightContainer world, int x, int y, int z);

    boolean updateLight(LightContainer world, int x, int y, int z, int newLight);

}
