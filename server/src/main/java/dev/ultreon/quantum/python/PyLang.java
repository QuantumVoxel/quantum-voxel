package dev.ultreon.quantum.python;

import net.fabricmc.loader.api.FabricLoader;
import org.graalvm.polyglot.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .option("python.SysPrefix", FabricLoader.getInstance().getGameDir().toString())
                .option("python.CoreHome", FabricLoader.getInstance().getGameDir().toString())
                .allowAllAccess(true).build();

        try {
            PyLoader.getInstance().init(FabricLoader.getInstance().getGameDir().resolve("mods"), context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PyLoader.getInstance().close();
            context.close();
        }));
    }
}
