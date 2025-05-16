package dev.ultreon.quantum.debug;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;

public class DebugFlags {
    private static boolean detectDebug() {
        GamePlatform gamePlatform = GamePlatform.get();
        return gamePlatform.detectDebug();
    }


    public static final boolean IS_RUNNING_IN_DEBUG = detectDebug();

    public static final DebugFlag WORLD_GEN = new DebugFlag(false, "worldGen");
    public static final DebugFlag LOG_POSITION_RESET_ON_CHUNK_LOAD = new DebugFlag(false, "logging.positionReset");
    public static final DebugFlag ORE_FEATURE = new DebugFlag(false, "worldGen.ore");
    public static final DebugFlag LOG_CHUNK_LOAD_FAILURE = new DebugFlag(true, "logging.chunkLoadFailure");
    public static final DebugFlag CHUNK_LOADER_DEBUG = new DebugFlag(false, "worldGen.chunkLoaderDebug");

    static {
        if (IS_RUNNING_IN_DEBUG)
            CommonConstants.LOGGER.warn("Running in debug mode.");
    }
}
