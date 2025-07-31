package dev.ultreon.quantum.entity.ai;

import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Navigator extends EntityAIComponent<LivingEntity> {
    private final LivingEntity entity;
    private Path currentPath;
    private int pathIndex;
    private @Nullable LivingEntity target;
    private final Vec3d tmp3D1 = new Vec3d();
    private final Vec3d tmp3D2 = new Vec3d();
    private final Vec3d tmp3D3 = new Vec3d();
    private final Vec3d tmp3D4 = new Vec3d();
    private final Vec3d tmp3D5 = new Vec3d();

    public Navigator(LivingEntity entity) {
        this.entity = entity;
    }

    public void tick() {
        if (currentPath == null) {
            return;
        }

        if (currentPath.isDone(currentPoint())) {
            done();
            return;
        }

        if (Objects.equals(this.currentPoint().position, this.currentPath.points().get(pathIndex))) {
            if (pathIndex == this.currentPath.points().size() - 1) {
                done();
                return;
            }

            pathIndex++;
        }

        Vec3d pos = this.currentPath.points().get(pathIndex);
        Vec3d curPos = entity.getPosition(tmp3D4);

        if (pos.x == curPos.x && pos.y == curPos.y && pos.z == curPos.z) {
            done();
            return;
        }

        if (pos.y > curPos.y) {
            entity.jump();
        }

        entity.moveTowards(pos.x, pos.y, pos.z, entity.getSpeed());
    }

    public void setPath(Path path) {
        this.currentPath = path;
        this.pathIndex = 0;
    }

    public boolean hasPath() {
        return currentPath != null;
    }

    public void stop() {
        this.currentPath = null;
        this.pathIndex = 0;
    }

    public void randomPath(RNG rng) {
        Vec3d position = entity.getPosition(tmp3D3);
        List<Vec3d> points = new ArrayList<>();
        points.add(position);
        for (int i = 0; i < rng.randint(1, 3); i++) {
            position = randomPos(rng, position);
            points.add(position);
        }

        this.setPath(new Path(points, currentPoint(), points.get(points.size() - 1)));
    }

    private Vec3d randomPos(RNG rng, Vec3d pos) {
        Direction[] directions = Direction.values();
        Vec3d dir = directions[rng.randint(0, 3)].vector();

        return new Vec3d(pos.x + dir.x, pos.y, pos.z + dir.z);
    }

    private void done() {
        currentPath = null;
    }

    private PathPoint currentPoint() {
        return new PathPoint(this.entity.getPosition(tmp3D1), this.entity.getVelocity(tmp3D2), this.entity.getLookVector(tmp3D3));
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        this.target = target;
    }

    @Override
    public boolean hasTarget() {
        return target != null && target.isAlive() && target.getHealth() > 0.0f && target.getPosition(tmp3D1).distanceSquared(entity.getPosition(tmp3D2)) > 16.0d;
    }

    @Override
    public void reset() {
        this.currentPath = null;
        this.pathIndex = 0;
        this.target = null;
    }

    private enum Direction {
        NORTH(0, -1),
        EAST(1, 0),
        SOUTH(0, 1),
        WEST(-1, 0);

        private final int x;
        private final int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Vec3d vector() {
            return new Vec3d(x, 0, y);
        }
    }
}
