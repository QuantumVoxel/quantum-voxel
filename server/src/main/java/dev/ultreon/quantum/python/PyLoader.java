package dev.ultreon.quantum.python;

import com.google.gson.Gson;
import dev.ultreon.quantum.FabricMod;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.LangLoader;
import dev.ultreon.quantum.graal.GraalLanguages;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipFile;

public class PyLoader implements LangLoader {
    private static final PyLoader INSTANCE = new PyLoader();
    private static final Logger LOGGER = LoggerFactory.getLogger(PyLoader.class);
    private final Map<String, PyMod> mods = new HashMap<>();
    private final Map<String, Runnable> pyDecos = new HashMap<>();
    private final Map<String, Value> scripts = new HashMap<>();

    @SuppressWarnings({"PyUnresolvedReferences", "PyInterpreter", "PyClassHasNoInit", "PyPep8Naming"})
    @Language("python")
    private static final String INTEROP_CHECK = """
            import org.slf4j.Logger as Slf4jLogger
            import org.slf4j.LoggerFactory as Slf4jLoggerFactory
            
            LOGGER = Slf4jLoggerFactory.getLogger(__name__)
            
            if __name__ == '__main__':
                LOGGER.info('Hello from Python!')
            
            """;

    public static PyLoader getInstance() {
        return INSTANCE;
    }

    public void register(String id, Runnable runnable) {
        pyDecos.put(id, runnable);
    }

    @Override
    public void init(Path path, Context context) throws IOException {
        if (!Files.exists(path.resolve("python"))) {
            Files.createDirectories(path.resolve("python"));
        }

        Value python = GraalLanguages.context.parse("python", INTEROP_CHECK);
        if (!python.canExecute()) {
            throw new IOException("Failed to execute python interop check script");
        }
        python.executeVoid();

        LOGGER.debug("Loading Python mods from " + path);
        try (var list = Files.list(path)) {
            list.filter(p -> p.toString().endsWith(".pyz")).forEach(p -> {
                try {
                    LOGGER.debug("Attempting to extract Python package: " + p);
                    extractPyz(p);
                } catch (Exception e) {
                    PyLang.LOGGER.error("Failed to load python file: {}", p, e);
                }
            });
        }
        try (var list = Files.list(path.resolve("python"))) {
            list.filter(Files::isDirectory).forEach(p -> {
                try {
                    LOGGER.debug("Loading Python mod: " + p);
                    Path modRoot = GamePlatform.get().getGameDir().resolve("mods/python/" + p.getFileName());
                    PyMod pyMod = new Gson().fromJson(Files.readString(modRoot.resolve("python.mod.json")), PyMod.class);
                    mods.put(pyMod.id(), pyMod);
                    pyMod.path = modRoot;

                    execute(modRoot);
                } catch (Exception e) {
                    PyLang.LOGGER.error("Failed to load python file: {}", p, e);
                }
            });
        }
    }

    public void initMods() {
        for (PyMod pyMod : mods.values()) {
            Path modRoot = pyMod.path;
            Path resolve = modRoot.resolve("__init__.py");
            if (!Files.exists(resolve)) {
                LOGGER.warn("Python mod doesn't have an __init__.py file: " + resolve);
                continue;
            }

            LOGGER.debug("Loading Python mod: " + pyMod.id + " from " + modRoot.getParent());

            try {
                LOGGER.debug("Loading Python mod: " + pyMod.id);
                Source source = Source.newBuilder("python", """
                                try:
                                    import %1$s
                                
                                    if hasattr(%1$s, "init"):
                                        %1$s.init()
                                except Exception as e:
                                    import traceback, sys
                                    raise Exception("Failed to execute init script: \\n" + traceback.format_exc())
                                """.formatted(modRoot.getFileName()), "__dyn_mod_init_" + pyMod.name + "__")
                        .mimeType("text/x-python")
                        .build();
                Value parse = GraalLanguages.context.parse(source);
                scripts.put(pyMod.id(), parse.execute());

                Runnable runnable = pyDecos.get(pyMod.id());
                if (runnable != null)
                    runnable.run();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load python mod: " + pyMod.id, e);
            }
        }
    }

    private void execute(Path modRoot) {
        try {
            Path preLaunchScript = modRoot.resolve("__pre_launch__.py");
            if (!Files.exists(preLaunchScript)) return;
            Value python = GraalLanguages.context.parse(Source.newBuilder("python", """
                    try:
                        import %1$s
                    
                        if hasattr(%1$s, "pre_launch"):
                            %1$s.pre_launch()
                    except Exception as e:
                        import traceback, sys
                        raise Exception("Failed to execute pre launch script: \\n" + traceback.format_exc())
                    """.formatted(modRoot.getFileName()), "__dyn_mod_init_" + modRoot.getFileName() + "__").mimeType("text/x-python").build());
            python.execute();
        } catch (Exception e) {
            PyLang.LOGGER.error("Failed to execute pre launch python file: {}", modRoot, e);
        }
    }

    private void extractPyz(Path p) throws IOException {
        Path resolve = GamePlatform.get().getGameDir().resolve("mods/python");
        Files.createDirectories(resolve);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                Files.walk(resolve).sorted().map(Path::toFile).forEach(File::delete);
//            } catch (IOException e) {
//                PyLang.LOGGER.error("Failed to delete python file: {}", p, e);
//            }
        }));

        try (ZipFile zipFile = new ZipFile(p.toFile())) {
            zipFile.stream().forEach(entry -> {
                try {
                    Path outputFile = GamePlatform.get().getGameDir().resolve("mods/python/" + entry.getName());
                    Path parent = outputFile.getParent();
                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(zipFile.getInputStream(entry), outputFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    PyLang.LOGGER.error("Failed to load python file: {}", entry.getName(), e);
                }
            });
        } catch (Exception e) {
            PyLang.LOGGER.error("Failed to load python file: {}", p, e);
        }
    }

    public void close() {

    }

    public Collection<PyMod> getMods() {
        return Collections.unmodifiableCollection(mods.values());
    }

    public boolean isModLoaded(String id) {
        return mods.containsKey(id);
    }

    public PyMod getMod(String id) {
        return mods.get(id);
    }
}
