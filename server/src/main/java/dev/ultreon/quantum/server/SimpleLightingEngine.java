package dev.ultreon.quantum.server;

import dev.ultreon.quantum.util.IVec3;
import dev.ultreon.quantum.util.IVec4;
import dev.ultreon.quantum.world.Direction;

import java.util.*;

public class SimpleLightingEngine implements LightingEngine {
    private final QuantumServer server;

    public SimpleLightingEngine(QuantumServer server) {
        this.server = server;
    }

    public void addSunLight(LightContainer world, int x, int y, int z) {
        int skyLight = world.getSkyLight(x, y, z);
        if (skyLight == 15) return;

        Stack<IVec4> queue = new Stack<>();
        queue.push(new IVec4(x, y, z, 15));
        while (!queue.isEmpty()) {
            IVec4 lightData = queue.pop();
            int xCoord = lightData.x;
            int yCoord = lightData.y;
            int zCoord = lightData.z;
            int lightValue = lightData.w;

            if (lightValue > 15) {
                lightValue = 15;
            }

            int oldLight = world.getSkyLight(xCoord, yCoord, zCoord);
            if (lightValue > oldLight) {
                world.setSkyLight(xCoord, yCoord, zCoord, lightValue);
            }
            for (Direction direction : Direction.values()) {
                int l = lightValue;
                IVec3 offset = direction.getOffset();
                int newX = xCoord + offset.getIntX();
                int newY = yCoord + offset.getIntY();
                int newZ = zCoord + offset.getIntZ();

                int curLight = world.getSkyLight(newX, newY, newZ);
                if (world.getLightBlockingHeight(newX, newZ) > newY) {
                    continue;
                }
                int lightReduction = world.getLightReduction(newX, newY, newZ);
                if (lightReduction <= 0) lightReduction = 1;
                l -= lightReduction;
                if (l < 0) l = 0;

                if (curLight > l) {
                    continue;
                }

                if (world.isOutOfWorldBounds(newX, newY, newZ)) continue;
                queue.push(new IVec4(newX, newY, newZ, l));
            }
        }
    }

    @Override
    public boolean addLight(LightContainer world, int x, int y, int z, int light) {
        boolean updated = false;
        Stack<IVec4> queue = new Stack<>();
        queue.push(new IVec4(x, y, z, light));
        while (!queue.isEmpty()) {
            IVec4 lightData = queue.pop();
            int xCoord = lightData.x;
            int yCoord = lightData.y;
            int zCoord = lightData.z;
            int lightValue = lightData.w;

            if (lightValue > 15) {
                lightValue = 15;
            }

            int oldLight = world.getBlockLight(xCoord, yCoord, zCoord);
            if (lightValue > oldLight) {
                updated = true;
            }
            world.setBlockLight(xCoord, yCoord, zCoord, lightValue);
            for (Direction direction : Direction.values()) {
                int l = lightValue;
                IVec3 offset = direction.getOffset();
                int newX = xCoord + offset.getIntX();
                int newY = yCoord + offset.getIntY();
                int newZ = zCoord + offset.getIntZ();

                int lightReduction = world.getLightReduction(newX, newY, newZ);
                int curLight = world.getBlockLight(newX, newY, newZ);
                if (lightReduction <= 0) lightReduction = 1;
                l -= lightReduction;
                if (l < 0) l = 0;
                if (curLight > l) {
                    continue;
                }

                if (world.isOutOfWorldBounds(newX, newY, newZ) || !world.isLoaded(newX, newY, newZ)) continue;
                queue.push(new IVec4(newX, newY, newZ, l));
            }
        }

        return updated;
    }

    @Override
    public boolean removeLight(LightContainer world, int x, int y, int z) {
        if (!world.isLoaded(x, y, z)) return false;

        int blockLight = world.getBlockLight(x, y, z);
        if (blockLight > 0) return false;
        world.setBlockLight(x, y, z, 0);

        // Check for other light sources within a 16x16 diamond shape
        Stack<IVec4> removalQueue = new Stack<>();
        Set<IVec3> checkedLights = new HashSet<>();
        List<IVec4> foundLights = new ArrayList<>();
        removalQueue.push(new IVec4(x, y, z, 0));
        while (!removalQueue.isEmpty()) {
            IVec4 removalData = removalQueue.pop();
            int xCoord = removalData.x;
            int yCoord = removalData.y;
            int zCoord = removalData.z;
            int distance = removalData.w;

            if (distance > 16) continue;

            for (Direction direction : Direction.values()) {
                int newX = xCoord + direction.getOffset().getIntX();
                int newY = yCoord + direction.getOffset().getIntY();
                int newZ = zCoord + direction.getOffset().getIntZ();

                if (world.isOutOfWorldBounds(newX, newY, newZ)) continue;
                int source = world.getSourceLight(newX, newY, newZ);
                if (source > 0 && !checkedLights.contains(new IVec3(newX, newY, newZ))) {
                    foundLights.add(new IVec4(newX, newY, newZ, source));
                    checkedLights.add(new IVec3(newX, newY, newZ));
                    if (distance + 1 > 16) continue;
                    removalQueue.push(new IVec4(newX, newY, newZ, distance + 1));
                }
            }
        }

        boolean updated = false;
        for (IVec4 light : foundLights) {
            updated |= this.addLight(world, light.x, light.y, light.z, light.w);
        }
        return updated;
    }

    public QuantumServer getServer() {
        return server;
    }

    @Override
    public boolean updateLight(LightContainer world, int x, int y, int z, int newLight) {
        int oldLight = world.getBlockLight(x, y, z);
        if (oldLight == newLight) return false;
        if (oldLight > newLight) this.removeLight(world, x, y, z);
        return this.addLight(world, x, y, z, newLight);
    }
}
