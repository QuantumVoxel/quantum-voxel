package dev.ultreon.quantum.world;

public enum ChunkLoadTicket {
    PLAYER(20),
    SPAWN(-1);

    private final int keepAliveTicks;

    ChunkLoadTicket(int keepAliveTicks) {
        this.keepAliveTicks = keepAliveTicks;
    }

    public int getKeepAliveTicks() {
        return keepAliveTicks;
    }

    public boolean isPermanent() {
        return this.keepAliveTicks == -1;
    }
}
