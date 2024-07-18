package dev.ultreon.quantum.js;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import org.graalvm.polyglot.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JsLang {
    static final Logger LOGGER = LoggerFactory.getLogger("JavascriptFabric");
    private static Context context;

    public static Context getContext() {
        return JsLang.context;
    }

    public void init() {
        Path modulesPath = GamePlatform.get().getGameDir().resolve("js").toAbsolutePath();
        if (Files.exists(modulesPath)) {
            // Delete recursively
            try {
                Files.walk(modulesPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                if (Files.exists(modulesPath))
                    Files.delete(modulesPath);
            } catch (IOException e) {
                LOGGER.error("Failed to delete js-modules", e);
            }


            // Recreate
            try {
                Files.createDirectories(modulesPath);
            } catch (IOException e) {
                LOGGER.error("Failed to create js-modules", e);
            }
        }

        context = Context.newBuilder()
                .option("js.nashorn-compat", "true")
                .option("log.file", "logs/latest-javascript.log")
                .option("js.commonjs-require-cwd", String.valueOf(modulesPath))
                .allowAllAccess(true).build();

        try {
            if (GamePlatform.get().isDesktop())
                JsLoader.getInstance().init(GamePlatform.get().getGameDir().resolve("mods"), context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            JsLoader.getInstance().close();
            context.close();
        }));
    }

    private void extract(ZipInputStream zipInputStream, java.nio.file.Path output) throws IOException {
        ZipEntry nextEntry = zipInputStream.getNextEntry();
        while (nextEntry != null) {
            Path path = output.resolve(nextEntry.getName());
            if (nextEntry.isDirectory()) {
                Files.createDirectories(path);
            } else {
                Files.createDirectories(path.getParent());
                Files.copy(zipInputStream, path, StandardCopyOption.REPLACE_EXISTING);
            }
            nextEntry = zipInputStream.getNextEntry();
        }
    }
}
