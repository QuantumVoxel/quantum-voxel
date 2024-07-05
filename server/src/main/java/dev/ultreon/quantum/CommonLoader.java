package dev.ultreon.quantum;

import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.events.ConfigEvents;
import dev.ultreon.quantum.text.icon.EmoteMap;
import dev.ultreon.quantum.text.icon.IconMap;
import dev.ultreon.quantum.util.ModLoadingContext;
import dev.ultreon.quantum.util.Env;

/**
 * Class responsible for initializing configuration entry points.
 */
public class CommonLoader {

    /**
     * Initialize configuration entry points.
     * @param loader the FabricLoader instance
     */
    public static void initConfigEntrypoints(GamePlatform loader) {
        loader.invokeEntrypoint(CraftyConfig.ENTRYPOINT_KEY, CraftyConfig.class, craftyConfig -> ModLoadingContext.withinContext(craftyConfig.getMod(), craftyConfig::load));

        // Trigger event for config load on the client side.
        ConfigEvents.LOAD.factory().onConfigLoad(Env.CLIENT);

        IconMap.register();
        EmoteMap.register();
    }
}
