package dev.ultreon.quantum.debug;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;

import java.lang.management.ManagementFactory;
import java.util.List;

public class DebugFlags {
    private static boolean detectDebug() {
        GamePlatform gamePlatform = GamePlatform.get();
        return gamePlatform.detectDebug();
    }



    public static final boolean IS_RUNNING_IN_DEBUG = detectDebug();

    public static final DebugFlag CHUNK_PACKET_DUMP = new DebugFlag(false);
    public static final DebugFlag CHUNK_BLOCK_DATA_DUMP = new DebugFlag(false);
    public static final DebugFlag WARN_CHUNK_BUILD_OVERLOAD = new DebugFlag(true);
    public static final DebugFlag INSPECTION_ENABLED = new DebugFlag(true); //! Only enable for debugging
    public static final DebugFlag DUMP_TEXTURE_ATLAS = new DebugFlag(true);
    public static final DebugFlag WORLD_GEN = new DebugFlag(false);
    public static final DebugFlag LOG_POSITION_RESET_ON_CHUNK_LOAD = new DebugFlag(false);
    public static final DebugFlag PACKET_LOGGING = new DebugFlag(true);
    public static final DebugFlag ORE_FEATURE = new DebugFlag(false);
    public static final DebugFlag LOG_OUT_OF_BOUNDS = new DebugFlag(false);

    static {
        if (IS_RUNNING_IN_DEBUG)
            CommonConstants.LOGGER.warn("Running in debug mode.");
    }
}
