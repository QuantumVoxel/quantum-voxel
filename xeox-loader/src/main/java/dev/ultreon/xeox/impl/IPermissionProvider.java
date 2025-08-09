package dev.ultreon.xeox.impl;

import dev.ultreon.xeox.impl.main.Main;

public interface IPermissionProvider {
    default void check(Mod mod, String permission) throws SecurityException {
        check(mod, permission, "The mod '" + mod.name() + "' does not have permission '" + permission + "', please contact the mod author. If you are the author of this mod, please add the permission to the mod's metadata.");
    }

    default void check(Mod mod, String permission, String message) throws SecurityException {
        if (!mod.permissions().contains(permission)) {
            Main.LOGGER.error("Mod '{}' does not have permission '{}', please contact the mod author. If you are the author of this mod, please add the permission to the mod's metadata.", mod.name(), permission);
            throw new SecurityException(message);
        }
    }

    default boolean requestPermission(Mod mod, String permission, Runnable runnable) throws SecurityException {
        throw new UnsupportedOperationException("This game doesn't support granting permissions.");
    }

    default boolean supportsGranting() {
        return false;
    }

    static boolean isRoot(String permission) {
        return permission.equals("*");
    }

    static String getType(String permission) {
        return permission.substring(0, permission.indexOf(':'));
    }

    static String getData(String permission) {
        return permission.substring(permission.indexOf(':') + 1);
    }
}
