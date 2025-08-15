package dev.ultreon.quantum;

import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.api.events.LoadingEvent;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.scripting.ScriptLoader;
import dev.ultreon.quantum.text.icon.EmoteMap;
import dev.ultreon.quantum.text.icon.IconMap;
import dev.ultreon.quantum.util.ModLoadingContext;

/**
 * Class responsible for initializing configuration entry points.
 */
public final class CommonLoader {
    private static final CommonLoader loader = new CommonLoader();
    private final ResourceManager resources = new ResourceManager("conent") {

        @Override
        protected void importGameResources() {
            GamePlatform.get().locateContentResources(this);
        }
    };

    private CommonLoader() {
    }

    public static CommonLoader get() {
        return loader;
    }

    public void init() {
        resources.reload();

        GamePlatform.get().load(resources);

        initConfigs();
        initNetLoggers();
        initChatImageMaps();
    }

    private static void initConfigs() {
        GamePlatform.get().invokeEntrypoint(CraftyConfig.ENTRYPOINT_KEY, CraftyConfig.class, craftyConfig -> ModLoadingContext.withinContext(craftyConfig.getMod(), craftyConfig::load));
        EventSystem.postDefault(new LoadingEvent.Configs(GamePlatform.get().getEnv()));
    }

    private static void initNetLoggers() {
//        KyroSlf4jLogger.set();
//        KyroNetSlf4jLogger.set();
    }

    private static void initChatImageMaps() {
        IconMap.register();
        EmoteMap.register();
    }

    /**
     * Initialize configuration entry points.
     * @param loader the FabricLoader instance
     */
    @Deprecated
    public static void initConfigEntrypoints(GamePlatform loader) {
        get().init();
    }

    public ResourceManager getContentResources() {
        return resources;
    }
}
