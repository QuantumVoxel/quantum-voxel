package dev.ultreon.xeox.impl.games.quantum;

import dev.ultreon.xeox.impl.IPermissionProvider;
import dev.ultreon.xeox.impl.Mod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dev.ultreon.xeox.impl.main.Main.LOGGER;

public class QuantumPermissionProvider implements IPermissionProvider {
    private final Map<String, ThreadLocal<Set<String>>> granted = new ConcurrentHashMap<>();

    @Override
    public void check(Mod mod, String permission, String message) throws SecurityException {
        if (!granted.containsKey(mod.modId())) {
            IPermissionProvider.super.check(mod, permission, message);
            return;
        }

        Set<String> strings = granted.get(mod.modId()).get();
        if (strings == null) {
            IPermissionProvider.super.check(mod, permission, message);
            return;
        }
        if (!strings.contains(permission)) {
            IPermissionProvider.super.check(mod, permission, message);
        }
    }

    @Override
    public boolean requestPermission(Mod mod, String permission, Runnable runnable) throws SecurityException {
        if (!granted.containsKey(mod.modId())) {
            granted.put(mod.modId(), ThreadLocal.withInitial(ConcurrentHashMap::newKeySet));
            return true;
        }
        synchronized (granted) {
            Set<String> strings = granted.get(mod.modId()).get();
            if (strings.contains(permission)) {
                return true;
            }
            if (!askUser(mod, permission)) {
                return false;
            }
            strings.add(permission);
            runnable.run();
            strings.remove(permission);
        }
        return true;
    }

    private boolean askUser(Mod mod, String permission) {
        InputStream resourceAsStream = getClass().getResourceAsStream("/quantum-permission-helper.jar");
        if (resourceAsStream == null) {
            System.err.println("Failed to load permission helper!");
            return false;
        }

        Path tempDirectory;
        Process proc = null;
        try {
            tempDirectory = Files.createTempDirectory("xeox-permission-helper");
            tempDirectory.toFile().deleteOnExit();

            Path jarFile = tempDirectory.resolve("permission-helper.jar");
            Files.copy(resourceAsStream, jarFile);
            proc = Runtime.getRuntime().exec(new String[]{
                    System.getProperty("java.home") + (System.getProperty("os.name").toLowerCase().contains("win") ? "\\bin\\java.exe" : "/bin/java"),
                    "-jar",
                    jarFile.toString(),
                    "Permission Helper",
                    "You are about to grant " + mod.name() + " the permission " + permission + ". Do you want to continue?"
            });
            proc.waitFor();
        } catch (IOException e) {
            System.err.println("Failed to create temporary directory for permission helper!");
            return false;
        } catch (InterruptedException e) {
            System.err.println("Failed to start permission helper!");
            proc.destroyForcibly();
            return false;
        }

        int i = proc.exitValue();
        if (i != 0) {
            LOGGER.warn("Permission helper finished with non-zero exit value {}", i);
            return false;
        }

        return true;
    }

    @Override
    public boolean supportsGranting() {
        return true;
    }
}
