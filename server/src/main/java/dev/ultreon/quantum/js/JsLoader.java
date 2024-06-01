package dev.ultreon.quantum.js;

import com.google.gson.Gson;
import dev.ultreon.quantum.LangLoader;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.SandboxPolicy;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsLoader implements LangLoader {
    private static final JsLoader INSTANCE = new JsLoader();
    private static final Logger LOGGER = LoggerFactory.getLogger(JsLoader.class);
    public static final String CONTENT_TYPE = "text/javascript";
    public static final String LOAD_FAIL_MESSAGE = "Failed to load javascript file: {}";
    private final Map<String, JsMod> mods = new HashMap<>();
    private final Map<String, Runnable> pyDecos = new HashMap<>();
    private final Map<String, Value> scripts = new HashMap<>();
    private List<String> javascriptPath = new ArrayList<>();

    public static JsLoader getInstance() {
        return INSTANCE;
    }

    public void register(String id, Runnable runnable) {
        pyDecos.put(id, runnable);
    }

    @Override
    public void init(Path path, Context context) throws IOException {
        Path resolve = path.resolve("js");
        if (Files.notExists(resolve)) {
            LOGGER.warn("Javascript mods folder not found: {}", resolve);
            Files.createDirectories(resolve);
            return;
        }
        try (var list = Files.list(resolve)) {
            list.filter(Files::isDirectory).forEach(p -> {
                Path modRoot = path.resolve(p.getFileName());
                Path resolved = modRoot.resolve("javascript.mod.json");

                if (Files.notExists(resolved)) return;
                try {
                    javascriptPath.add(modRoot.toAbsolutePath().toString());
                    JsMod pyMod = new Gson().fromJson(Files.readString(resolved), JsMod.class);
                    mods.put(pyMod.id(), pyMod);
                    pyMod.path = modRoot;

                    execute(modRoot);
                } catch (Exception e) {
                    JsLang.LOGGER.error(LOAD_FAIL_MESSAGE, p, e);
                }
            });
        }
    }

    public void initMods() {
        for (JsMod jsMod : mods.values()) {
            Path modRoot = jsMod.path;
            Path resolve = modRoot.resolve(jsMod.id() + "/index.mjs");
            if (!Files.exists(resolve)) continue;

            Context context = Context.newBuilder("js")
                    .allowAllAccess(true)
                    .sandbox(SandboxPolicy.TRUSTED)
                    .option("js.nashorn-compat", "true")
                    .useSystemExit(false)
                    .currentWorkingDirectory(modRoot.toAbsolutePath())
                    .build();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    context.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to close javascript context", e);
                }
            }));

            try {
                Source source = Source.newBuilder("js", resolve.toFile())
                        .mimeType(CONTENT_TYPE)
                        .build();
                Value parse = context.parse(source);
                scripts.put(jsMod.id(), parse.execute());

                Runnable runnable = pyDecos.get(jsMod.id());
                if (runnable != null)
                    runnable.run();
            } catch (IOException e) {
                throw new ModLoadException(e);
            }
        }
    }

    private void execute(Path modRoot) {
        Context context = Context.newBuilder("javascript")
                .allowAllAccess(true)
                .sandbox(SandboxPolicy.TRUSTED)
                .option("js.nashorn-compat", "true")
                .useSystemExit(false)
                .currentWorkingDirectory(modRoot.toAbsolutePath())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                context.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close javascript context", e);
            }
        }));

        try(Stream<Path> list1 = Files.list(modRoot)) {
            List<Path> list = list1.collect(Collectors.toList());
            for (Path p : list) {
                if (p.toString().endsWith(".mjs")) {
                    Source source = Source.newBuilder("js", p.toFile())
                            .name(p.getFileName().toString())
                            .mimeType(CONTENT_TYPE)
                            .build();

                    LOGGER.info("Loading: " + p.getFileName());
                    context.parse(source);
                }
            }

            Path preLaunchScript = modRoot.resolve("pre-launch.mjs");
            if (!Files.exists(preLaunchScript)) return;
            Value javascript = context.parse(Source.newBuilder("js", preLaunchScript.toFile()).mimeType(CONTENT_TYPE).build());
            javascript.execute();
        } catch (Exception e) {
            JsLang.LOGGER.error(LOAD_FAIL_MESSAGE, modRoot, e);
        }
    }

    public void close() {

    }

    public Collection<JsMod> getMods() {
        return Collections.unmodifiableCollection(mods.values());
    }
}
