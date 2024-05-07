package dev.ultreon.quantum.world;

import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.Identifier;

public class Location {
    public Identifier world;
    public double x;
    public double y;
    public double z;
    public float xRot;
    public float yRot;

    public Location(Identifier world, double x, double y, double z, float xRot, float yRot) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public Location(World world, double x, double y, double z, float xRot, float yRot) {
        this(world.getDimension().getId(), x, y, z, xRot, yRot);
    }

    public Location(double x, double y, double z, float xRot, float yRot) {
        this((Identifier) null, x, y, z, xRot, yRot);
    }

    public Location(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public Location(ServerWorld dimension, double x, double y, double z) {
        this(dimension, x, y, z, 0, 0);
    }

    public Location cpy() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    public ServerWorld getSeverWorld() {
        QuantumServer server = QuantumServer.get();
        if (server == null) return null;

        return server.getWorld(this.world);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }
}
