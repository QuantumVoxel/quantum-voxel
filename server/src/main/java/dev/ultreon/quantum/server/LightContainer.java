package dev.ultreon.quantum.server;

public interface LightContainer {
    int getBlockLight(int x, int y, int z);
    int getSkyLight(int x, int y, int z);
    
    void setBlockLight(int x, int y, int z, int light);
    void setSkyLight(int x, int y, int z, int light);

    int getLightReduction(int x, int y, int z);

    boolean isOutOfWorldBounds(int x, int y, int z);

    boolean isLoaded(int x, int y, int z);

    int getSourceLight(int x, int y, int z);

    int getLightBlockingHeight(int x, int z);
}
