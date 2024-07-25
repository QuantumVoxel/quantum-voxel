/*
Copyright 2024 Kevin James

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.world.gen.noise.SimplexNoise;

public class VoxelTerrain {

    public static Model prefab;

    public Model PREFAB;
    public TerrainNode root;

    public Camera camera;

    public int camX = 0;
    public int camY = 0;
    public int camZ = 0;

    public static SimplexNoise noise = new SimplexNoise(16, 0.5, 0);

    public static final Array<TerrainNode> GENERATING_QUEUE = new Array<>();
    public static final Array<TerrainNode> MESHING_QUEUE = new Array<>();

    public void create() {
        // Initialize the noise and root node here
    }

    public void render() {
        root.updateVisibility();
        int cx = (int) (camera.position.x / TerrainNode.TERRAIN_SIZE);
        int cy = (int) (camera.position.y / TerrainNode.TERRAIN_SIZE);
        int cz = (int) (camera.position.z / TerrainNode.TERRAIN_SIZE);

        if (camX != cx || camY != cy || camZ != cz) {
            root.update((int) camera.position.x, (int) camera.position.y, (int) camera.position.z);
            camX = cx;
            camY = cy;
            camZ = cz;
        }

        if (GENERATING_QUEUE.size > 0) {
            int closeIdx = 0;
            int smallestSize = root.size;
            float closeDist = 1e30f;
            for (int i = 0; i < GENERATING_QUEUE.size; i++) {
                Vector3 pos = new Vector3(GENERATING_QUEUE.get(i).x, GENERATING_QUEUE.get(i).y, GENERATING_QUEUE.get(i).z);
                float d = pos.dst(camera.position);
                if (GENERATING_QUEUE.get(i).size <= smallestSize) {
                    if (GENERATING_QUEUE.get(i).size < smallestSize) {
                        closeDist = 1e30f;
                    }
                    if (d <= closeDist) {
                        closeDist = d;
                        closeIdx = i;
                        smallestSize = GENERATING_QUEUE.get(i).size;
                    }
                }
            }
            TerrainNode generating = GENERATING_QUEUE.get(closeIdx);
            GENERATING_QUEUE.removeIndex(closeIdx);

            if (generating.modelInstance == null) {
                return;
            }
            generating.generate();
            MESHING_QUEUE.add(generating);
        }

        if (MESHING_QUEUE.size > 0) {
            int closeIdx = 0;
            int smallestSize = root.size;
            float closeDist = 1e30f;
            for (int i = 0; i < MESHING_QUEUE.size; i++) {
                Vector3 pos = new Vector3(MESHING_QUEUE.get(i).x, MESHING_QUEUE.get(i).y, MESHING_QUEUE.get(i).z);
                float d = pos.dst(camera.position);
                if (MESHING_QUEUE.get(i).size <= smallestSize) {
                    if (MESHING_QUEUE.get(i).size < smallestSize) {
                        closeDist = 1e30f;
                    }
                    if (d <= closeDist) {
                        closeDist = d;
                        closeIdx = i;
                        smallestSize = MESHING_QUEUE.get(i).size;
                    }
                }
            }
            TerrainNode meshing = MESHING_QUEUE.get(closeIdx);
            if (meshing.modelInstance != null) {
                MESHING_QUEUE.removeIndex(closeIdx);
                meshing.buildMesh();
            }
        }
    }
}
