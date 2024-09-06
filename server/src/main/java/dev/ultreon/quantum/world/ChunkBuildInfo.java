package dev.ultreon.quantum.world;

import dev.ultreon.quantum.network.PacketIO;

public class ChunkBuildInfo {
    public long buildDuration;

    public ChunkBuildInfo() {

    }

    public ChunkBuildInfo(PacketIO buffer) {
        this.buildDuration = buffer.readLong();
    }

    public void toBytes(PacketIO buffer) {
        buffer.writeLong(this.buildDuration);
    }
}
