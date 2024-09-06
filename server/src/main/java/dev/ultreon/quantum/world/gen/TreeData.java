package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.util.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class TreeData {
    public List<Vec2i> treePositions = new ArrayList<>();
    public final List<Vec3i> treeLeavesSolid = new ArrayList<>();
}
