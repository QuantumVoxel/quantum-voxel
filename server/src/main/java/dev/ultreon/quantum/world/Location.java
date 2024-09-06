package dev.ultreon.quantum.world;

import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;

public class Location {
    public NamespaceID dimension;
    public double x;
    public double y;
    public double z;
    public float xRot;
    public float yRot;

    public Location(NamespaceID dimension, double x, double y, double z, float xRot, float yRot) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public Location(WorldAccess dimension, double x, double y, double z, float xRot, float yRot) {
        this(dimension.getDimension().id(), x, y, z, xRot, yRot);
    }

    public Location(double x, double y, double z, float xRot, float yRot) {
        this((NamespaceID) null, x, y, z, xRot, yRot);
    }

    public Location(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public Location(ServerWorld dimension, double x, double y, double z) {
        this(dimension, x, y, z, 0, 0);
    }

    public Location cpy() {
        return new Location(this.dimension, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    public ServerWorld getServerWorld() {
        QuantumServer server = QuantumServer.get();
        if (server == null) return null;

        return server.getWorld(this.dimension);
    }

    public BlockVec getBlockVec() {
        return new BlockVec(this.x, this.y, this.z, BlockVecSpace.WORLD);
    }
}
