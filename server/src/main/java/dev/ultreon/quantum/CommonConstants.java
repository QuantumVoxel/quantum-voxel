package dev.ultreon.quantum;

import com.google.gson.Gson;
import de.marhali.json5.Json5;
import de.marhali.json5.Json5Options;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import dev.ultreon.quantum.util.NamespaceID;

public class CommonConstants {
    public static final String EX_NOT_ON_RENDER_THREAD = "Current thread is not the rendering thread.";
    public static final String EX_FAILED_TO_LOAD_CONFIG = "Failed to load config file!";
    public static final String EX_FAILED_TO_SEND_PACKET = "Failed to send packet:";
    public static final String EX_INVALID_DATA = "Invalid data";
    public static final String EX_ARRAY_TOO_LARGE = "Array too large, max = %d, actual = %d";
    public static final String NAMESPACE = "quantum";
    public static final Gson GSON = new Gson();
    public static final Json5 JSON5 = Json5.builder(builder -> {
        // Setup JSON5 options
        builder.prettyPrinting();
        builder.indentFactor(4);
        builder.allowInvalidSurrogate();

        return builder.build();
    });
    public static final Json5Options JSON5_OPTIONS = Json5Options.builder()
            .prettyPrinting()
            .indentFactor(4)
            .allowInvalidSurrogate()
            .quoteless()
            .build();
    public static final int MAX_BLOCK_REGISTRY = 128;
    public static final NamespaceID DEFAULT_FONT = new NamespaceID("quantium");

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
