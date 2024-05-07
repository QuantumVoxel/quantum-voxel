package dev.ultreon.quantum.world.gen;

import dev.ultreon.libs.commons.v0.vector.Vec2i;
import dev.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class TreeData {
    public List<Vec2i> treePositions = new ArrayList<>();
    public final List<Vec3i> treeLeavesSolid = new ArrayList<>();
}
