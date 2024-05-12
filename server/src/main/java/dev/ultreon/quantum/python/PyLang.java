package dev.ultreon.quantum.python;

import dev.ultreon.quantum.GamePlatform;
import org.graalvm.polyglot.Context;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;

import java.io.IOException;

public class PyLang {
    static final Logger LOGGER = LoggerFactory.getLogger("PythonFabric");
    private static Context context;

    public static Context getContext() {
        return PyLang.context;
    }

    public void init() {
        context = Context.newBuilder()
                .option("python.EmulateJython", "true")
                .option("log.file", "logs/latest-python.log")
                .option("python.SysPrefix", GamePlatform.get().getGameDir().toString())
                .option("python.CoreHome", GamePlatform.get().getGameDir().toString())
                .allowAllAccess(true).build();

        try {
            if (GamePlatform.get().isDesktop())
                PyLoader.getInstance().init(GamePlatform.get().getGameDir().resolve("mods"), context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PyLoader.getInstance().close();
            context.close();
        }));
    }
}
