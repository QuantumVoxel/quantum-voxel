package dev.ultreon.quantum.graal;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.FileSystem;
import org.graalvm.polyglot.io.IOAccess;

import javax.management.MBeanInfo;

public class GraalLanguages {
    public static final Context context = Context.newBuilder("python", "js")
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowCreateProcess(false)
            .allowCreateThread(true)
            .allowExperimentalOptions(true)
            .allowHostClassLoading(true)
            .allowInnerContextOptions(true)
            .allowHostAccess(HostAccess.ALL)
            .allowEnvironmentAccess(EnvironmentAccess.INHERIT)
            .allowValueSharing(true)
            .allowNativeAccess(true)
            .environment("JAVA_HOME", System.getProperty("java.home"))
            .environment("PATH", System.getenv("PATH"))
            .environment("PYTHONPATH", GamePlatform.get().getGameDir().resolve("mods/python").toAbsolutePath().toString())
            .environment("PYTHONIOENCODING", "utf-8")
            .environment("FABRIC_DEVELOPMENT", GamePlatform.get().isDevEnvironment() ? "true" : "false")
            .environment("GAME_DIR", GamePlatform.get().getGameDir().toAbsolutePath().toString())
            .allowIO(IOAccess.newBuilder().fileSystem(FileSystem.newDefaultFileSystem()).build())
            .sandbox(SandboxPolicy.TRUSTED)
            .option("python.EmulateJython", "true")
            .option("python.PythonPath", GamePlatform.get().getGameDir().resolve("mods/python").toAbsolutePath().toString())
            .option("js.nashorn-compat", "true")
            .option("js.esm-eval-returns-exports", "true")
            .option("js.ecmascript-version", "latest")
            .option("log.file", GamePlatform.get().getGameDir().resolve("logs/latest-graal.log").toAbsolutePath().toString())
            .useSystemExit(false)
            .currentWorkingDirectory(GamePlatform.get().getGameDir().resolve("mods/js").toAbsolutePath())
            .build();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                context.close();
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to close python context", e);
            }
        }));
    }
}
