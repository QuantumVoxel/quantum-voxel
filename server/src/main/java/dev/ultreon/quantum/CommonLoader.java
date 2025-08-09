package dev.ultreon.quantum;

import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.api.events.LoadingEvent;
import dev.ultreon.quantum.config.crafty.CraftyConfig;
import dev.ultreon.quantum.di.DependencyContainer;
import dev.ultreon.quantum.network.system.KyroNetSlf4jLogger;
import dev.ultreon.quantum.network.system.KyroSlf4jLogger;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.scripting.ScriptLoader;
import dev.ultreon.quantum.text.icon.EmoteMap;
import dev.ultreon.quantum.text.icon.IconMap;
import dev.ultreon.quantum.util.ModLoadingContext;

/**
 * Class responsible for initializing configuration entry points.
 */
public final class CommonLoader {
    private final ResourceManager resources = new ResourceManager("conent") {

        @Override
        protected void importGameResources() {
            GamePlatform.get().locateContentResources(this);
        }
    };
    private final ScriptLoader scriptLoader = new ScriptLoader();

    private CommonLoader() {
    }

    public void init() {
        resources.reload();

        scriptLoader.reload(resources);

        initConfigs();
        initNetLoggers();
        initChatImageMaps();
    }

    private static void initConfigs() {
        GamePlatform.get().invokeEntrypoint(CraftyConfig.ENTRYPOINT_KEY, CraftyConfig.class, craftyConfig -> ModLoadingContext.withinContext(craftyConfig.getMod(), craftyConfig::load));
        EventSystem.postDefault(new LoadingEvent.Configs(GamePlatform.get().getEnv()));
    }

    private static void initNetLoggers() {
        KyroSlf4jLogger.set();
        KyroNetSlf4jLogger.set();
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
        DependencyContainer.getInstance().resolve(CommonLoader.class).init();
    }

    public ResourceManager getContentResources() {
        return resources;
    }
}
