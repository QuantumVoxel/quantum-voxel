package dev.ultreon.quantum.dedicated;

import dev.ultreon.quantum.config.crafty.*;
import dev.ultreon.quantum.world.World;

@ConfigInfo(fileName = "server")
public class ServerConfig extends CraftyConfig {
    @ConfigEntry(path = "hosting.hostname", comment = "The hostname to use for the server.")
    @RequiresRestart
    public static String hostname = "localhost";

    @ConfigEntry(path = "hosting.port", comment = "The port to use for the server.")
    @RequiresRestart
    public static int port = 38800;

    @ConfigEntry(path = "hosting.path", comment = "The path to host the server at")
    public static String path = "quantum-server";

    @ConfigEntry(path = "antiCheat.allowFlying", comment = "Whether to allow unauthorized players to fly.")
    public static boolean allowFlying = false;

    @ConfigEntry(path = "security.maxPlayers", comment = "The maximum number of players allowed on the server.")
    public static int maxPlayers = 10;

    @ConfigEntry(path = "server.renderDistance", comment = "The render distance of the server.")
    @Ranged(min = World.CS * 2, max = 256)
    public static int renderDistance = 128;
}
