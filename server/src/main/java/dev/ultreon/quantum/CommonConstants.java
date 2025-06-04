package dev.ultreon.quantum;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.rng.JavaRNG;

import java.time.format.DateTimeFormatter;

public class CommonConstants {
    public static final String EX_NOT_ON_RENDER_THREAD = "Current thread is not the rendering thread.";
    public static final String EX_FAILED_TO_LOAD_CONFIG = "Failed to load config file!";
    public static final String EX_FAILED_TO_SEND_PACKET = "Failed to send packet:";
    public static final String EX_INVALID_DATA = "Invalid data";
    public static final String EX_ARRAY_TOO_LARGE = "Array too large, max = %d, actual = %d";
    public static final String NAMESPACE = "quantum";
    public static final Json JSON5 = new Json(JsonWriter.OutputType.minimal);
    public static final JsonWriter.OutputType JSON5_OPTIONS = JsonWriter.OutputType.minimal;
    public static final NamespaceID DEFAULT_FONT = new NamespaceID("quantium");
    
    // Client Vec3D
    public static final Vec3d VEC3D_0_C = new Vec3d();
    public static final Vec3d VEC3D_1_C = new Vec3d();
    public static final Vec3d VEC3D_2_C = new Vec3d();
    public static final Vec3d VEC3D_3_C = new Vec3d();
    
    // Server Vec3D
    public static final Vec3d VEC3D_0_S = new Vec3d();
    public static final Vec3d VEC3D_1_S = new Vec3d();
    public static final Vec3d VEC3D_2_S = new Vec3d();
    public static final Vec3d VEC3D_3_S = new Vec3d();

    // Client Vec3f
    public static final Vec3f VEC3F_0_C = new Vec3f();
    public static final Vec3f VEC3F_1_C = new Vec3f();
    public static final Vec3f VEC3F_2_C = new Vec3f();
    public static final Vec3f VEC3F_3_C = new Vec3f();
    
    // Server Vec3f
    public static final Vec3f VEC3F_0_S = new Vec3f();
    public static final Vec3f VEC3F_1_S = new Vec3f();
    public static final Vec3f VEC3F_2_S = new Vec3f();
    public static final Vec3f VEC3F_3_S = new Vec3f();

    // Client Vec3i
    public static final Vec3i VEC3I_0_C = new Vec3i();
    public static final Vec3i VEC3I_1_C = new Vec3i();
    public static final Vec3i VEC3I_2_C = new Vec3i();
    public static final Vec3i VEC3I_3_C = new Vec3i();
    
    // Server Vec3i
    public static final Vec3i VEC3I_0_S = new Vec3i();
    public static final Vec3i VEC3I_1_S = new Vec3i();
    public static final Vec3i VEC3I_2_S = new Vec3i();
    public static final Vec3i VEC3I_3_S = new Vec3i();

    // Client Vector3
    public static final Vector3 VECTOR3_0_C = new Vector3();
    public static final Vector3 VECTOR3_1_C = new Vector3();
    public static final Vector3 VECTOR3_2_C = new Vector3();
    public static final Vector3 VECTOR3_3_C = new Vector3();
    
    // Server Vector3
    public static final Vector3 VECTOR3_0_S = new Vector3();
    public static final Vector3 VECTOR3_1_S = new Vector3();
    public static final Vector3 VECTOR3_2_S = new Vector3();
    public static final Vector3 VECTOR3_3_S = new Vector3();

    // Client Vec2D
    public static final Vec2d VEC2D_0_C = new Vec2d();
    public static final Vec2d VEC2D_1_C = new Vec2d();
    public static final Vec2d VEC2D_2_C = new Vec2d();
    public static final Vec2d VEC2D_3_C = new Vec2d();
    
    // Server Vec2D
    public static final Vec2d VEC2D_0_S = new Vec2d();
    public static final Vec2d VEC2D_1_S = new Vec2d();
    public static final Vec2d VEC2D_2_S = new Vec2d();
    public static final Vec2d VEC2D_3_S = new Vec2d();

    // Client Vec2f
    public static final Vec2f VEC2F_0_C = new Vec2f();
    public static final Vec2f VEC2F_1_C = new Vec2f();
    public static final Vec2f VEC2F_2_C = new Vec2f();
    public static final Vec2f VEC2F_3_C = new Vec2f();
    
    // Server Vec2f
    public static final Vec2f VEC2F_0_S = new Vec2f();
    public static final Vec2f VEC2F_1_S = new Vec2f();
    public static final Vec2f VEC2F_2_S = new Vec2f();
    public static final Vec2f VEC2F_3_S = new Vec2f();

    // Client Vec2i
    public static final Vec2i VEC2I_0_C = new Vec2i();
    public static final Vec2i VEC2I_1_C = new Vec2i();
    public static final Vec2i VEC2I_2_C = new Vec2i();
    public static final Vec2i VEC2I_3_C = new Vec2i();
    
    // Server Vec2i
    public static final Vec2i VEC2I_0_S = new Vec2i();
    public static final Vec2i VEC2I_1_S = new Vec2i();
    public static final Vec2i VEC2I_2_S = new Vec2i();
    public static final Vec2i VEC2I_3_S = new Vec2i();

    // Client Vector2
    public static final Vector2 VECTOR2_0_C = new Vector2();
    public static final Vector2 VECTOR2_1_C = new Vector2();
    public static final Vector2 VECTOR2_2_C = new Vector2();
    public static final Vector2 VECTOR2_3_C = new Vector2();
    
    // Server Vector2
    public static final Vector2 VECTOR2_0_S = new Vector2();
    public static final Vector2 VECTOR2_1_S = new Vector2();
    public static final Vector2 VECTOR2_2_S = new Vector2();
    public static final Vector2 VECTOR2_3_S = new Vector2();
    public static final JavaRNG RANDOM = new JavaRNG();
    public static final int DEFAULT_LOD_LEVEL = 0;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final JsonReader JSON_READ = new JsonReader();
    public static final int MAX_BLOCK_REGISTRY = 256;
    public static final int GENERATOR_ID = 0;

    private CommonConstants() {

    }

    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumVoxel");

    public static String strId(String outlineCursor) {
        return NAMESPACE + ":" + outlineCursor;
    }

    public static NamespaceID id(String path) {
        return new NamespaceID(NAMESPACE, path);
    }
}
