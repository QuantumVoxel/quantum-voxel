package dev.ultreon.quantum;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.rng.JavaRNG;

import java.time.format.DateTimeFormatter;

public class CommonConstants {
    public static final String EX_NOT_ON_RENDER_THREAD = "Current thread is not the rendering thread.";
    public static final String EX_INVALID_DATA = "Invalid data";
    public static final String EX_ARRAY_TOO_LARGE = "Array too large, max = %d, actual = %d";
    public static final String NAMESPACE = "quantum";
    public static final Json JSON5 = new Json(JsonWriter.OutputType.minimal);
    public static final NamespaceID DEFAULT_FONT = new NamespaceID("quantium");
    
    // Client Vec3D
    public static final Vec3d VEC3D = new Vec3d();

    public static final JavaRNG RANDOM = new JavaRNG();
    public static final int DEFAULT_LOD_LEVEL = 0;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final JsonReader JSON_READ = new JsonReader();
    public static final int MAX_BLOCK_REGISTRY = 256;
    public static final int GENERATOR_ID = 0;
    public static final String VERSION = "0.2.0-alpha.2";

    private CommonConstants() {

    }

    public static final Logger LOGGER = LoggerFactory.getLogger("QuantumVoxel");

    public static String strId(String outlineCursor) {
        return NAMESPACE + ":" + outlineCursor;
    }

    public static NamespaceID id(String path) {
        return new NamespaceID(NAMESPACE, path);
    }

    public static String getGameVersion() {
        return VERSION;
    }
}
