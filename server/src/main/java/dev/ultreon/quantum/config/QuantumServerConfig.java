package dev.ultreon.quantum.config;

import dev.ultreon.quantum.config.crafty.ConfigEntry;
import dev.ultreon.quantum.config.crafty.ConfigInfo;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.config.crafty.Ranged;

@ConfigInfo(fileName = "quantum_server")
public class QuantumServerConfig extends CraftyConfig {
    @ConfigEntry(path = "general.renderDistance", comment = "The render distance of chunks in the game.")
    @Ranged(min = 4, max = 32)
    public static int renderDistance = 16;

    @ConfigEntry(path = "general.entityRenderDistance", comment = "The render distance of entities.")
    @Ranged(min = 4, max = 32)
    public static int entityRenderDistance = 12;

    @ConfigEntry(path = "general.autoSaveInterval", comment = "The interval in seconds between automatic saves.")
    @Ranged(min = 1, max = 3600)
    public static int autoSaveInterval = 60;

    @ConfigEntry(path = "general.initialAutoSaveDelay", comment = "The initial delay in seconds between automatic saves.")
    @Ranged(min = 1, max = 3600)
    public static int initialAutoSaveDelay = 120;

    @ConfigEntry(path = "general.debugChunkPacketDump", comment = "Whether to dump chunk packets to the console.")
    public static boolean debugChunkPacketDump = false;

    @ConfigEntry(path = "general.debugBlockDataDump", comment = "Whether to dump block data to the console.")
    public static boolean debugBlockDataDump = false;

    @ConfigEntry(path = "general.debugWarnChunkBuildOverload", comment = "Whether to warn when a chunk build overload occurs.")
    public static boolean debugWarnChunkBuildOverload = false;
}
