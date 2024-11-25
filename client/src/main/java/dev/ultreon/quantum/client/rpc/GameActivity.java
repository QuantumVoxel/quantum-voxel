package dev.ultreon.quantum.client.rpc;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

/**
 * Enum representing different game activities.
 */
public enum GameActivity {
    MAIN_MENU("Main menu"),
    SINGLEPLAYER("Playing singleplayer"),
    MULTIPLAYER("Playing multiplayer", () -> {
        QuantumClient client = QuantumClient.get();
        if (client.serverInfo == null || ClientConfig.hideActiveServerFromRPC) {
            return null;
        }
        return "On " + client.serverInfo.name();
    });

    private final String displayName;
    private final Callable<@Nullable String> description;

    /**
     * Constructor for GameActivity with display name.
     * @param displayName the display name of the activity
     */
    GameActivity(String displayName) {
        this(displayName, () -> null);
    }

    /**
     * Constructor for GameActivity with display name and description.
     * @param displayName the display name of the activity
     * @param description the description of the activity
     */
    GameActivity(String displayName, Callable<@Nullable String> description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get the display name of the activity.
     * @return the display name
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Get the description of the activity.
     * @return the description
     * @throws Exception if an error occurs while getting the description
     */
    public @Nullable String getDescription() throws Exception {
        return this.description.call();
    }
}
