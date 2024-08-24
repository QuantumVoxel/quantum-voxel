package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.util.PosOutOfBoundsException;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;

public interface ClientChunkAccess extends Disposable, ChunkAccess {

    float getLightLevel(int x, int y, int z) throws PosOutOfBoundsException;

    float getSunlightLevel(int x, int y, int z);

    float getBlockLightLevel(int x, int y, int z);

    ChunkVec getVec();

    boolean isInitialized();

    void revalidate();

    ClientWorldAccess getWorld();

    int getSunlight(Vec3i pos);

    int getBlockLight(Vec3i pos);

    Vector3 getRenderOffset();

    float getBrightness(int lightLevel);

    ModelInstance addModel(BlockVec blockVec, ModelInstance modelInstance);

    BlockState get(Vec3i tmp3i);

}
