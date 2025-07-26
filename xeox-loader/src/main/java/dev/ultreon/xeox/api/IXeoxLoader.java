package dev.ultreon.xeox.api;

import dev.ultreon.xeox.XeoxLoaderProvider;

import java.util.List;
import java.util.function.Consumer;

public interface IXeoxLoader {
    /**
     * Returns the mod's information by its provided ID.
     * This function will throw a {@link SecurityException} if access to the mod is prohibited.
     *
     * @param modId the mod ID.
     * @return the mod's information.
     * @throws SecurityException if access to the mod is prohibited.
     */
    IMod getMod(String modId) throws SecurityException;

    /**
     * Returns true if the mod with the provided ID is loaded.
     *
     * @param modId the id of the mod to check whether it's loaded.
     * @return true if the mod is loaded.
     */
    boolean isModLoaded(String modId);

    /**
     * Gets the mod's info by class.
     *
     * @param clazz the mod's class.
     * @return the mod information.
     */
    IMod getModByClass(Class<?> clazz);

    /**
     * Returns all accessible mods by the current classloader.
     */
    List<IMod> getMods();

    /**
     * This returns the id of all mods.
     */
    List<String> getModIds();

    void requestPermission(String permission, Runnable runnable);

    static IXeoxLoader get() {
        return XeoxLoaderProvider.get();
    }

    <T> void invokeEntrypoints(String type, Class<T> initClass, Consumer<T> initializer);

    boolean isDevEnvironment();

    Environment getEnvironment();

    IPath getConfigDir();

    IPath getGameDir();

    String getGameVersion();

    int choose(String title, String message, String[] options);
}
