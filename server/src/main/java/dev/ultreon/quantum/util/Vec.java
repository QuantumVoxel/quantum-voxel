package dev.ultreon.quantum.util;

public class Vec extends Vec3d implements Point {
    public Vec(double x, double y, double z) {
        super(x, y, z);
    }

    public Vec(Vec2d vec, double z) {
        super(vec, z);
    }

    public Vec(double x, Vec2d vec) {
        super(x, vec);
    }

    public Vec(Vec3i vec) {
        super(vec);
    }

    public Vec(Vec3f vec) {
        super(vec);
    }

    public Vec(Vec3d vec) {
        super(vec);
    }

    public Vec(Point p) {
        super(p);
    }

    public Vec() {
    }

    @Override
    public double dst(Point point) {
        return super.dst(point.asVec());
    }
}
