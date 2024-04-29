package com.ultreon.quantum.server.dedicated;

import com.ultreon.quantum.config.crafty.ConfigEntry;
import com.ultreon.quantum.config.crafty.ConfigInfo;
import com.ultreon.quantum.config.crafty.CraftyConfig;
import com.ultreon.quantum.config.crafty.RequiresRestart;

@ConfigInfo(fileName = "server")
public class ServerConfig extends CraftyConfig {
    @ConfigEntry(path = "hosting.hostname", comment = "The hostname to use for the server.")
    @RequiresRestart
    public static String hostname = "localhost";

    @ConfigEntry(path = "hosting.port", comment = "The port to use for the server.")
    @RequiresRestart
    public static int port = 38800;

    @ConfigEntry(path = "antiCheat.allowFlying", comment = "Whether to allow unauthorized players to fly.")
    public static boolean allowFlying = false;

    @ConfigEntry(path = "security.maxPlayers", comment = "The maximum number of players allowed on the server.")
    public static int maxPlayers = 10;

    @ConfigEntry(path = "server.renderDistance", comment = "The render distance of the server.")
    public static int renderDistance;
}
