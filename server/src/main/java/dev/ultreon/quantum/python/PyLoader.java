package dev.ultreon.quantum.python;

import com.google.gson.Gson;
import dev.ultreon.quantum.GamePlatform;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.SandboxPolicy;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class PyLoader {
    private static final PyLoader INSTANCE = new PyLoader();
    private static final Logger LOGGER = LoggerFactory.getLogger(PyLoader.class);
    private final Map<String, PyMod> mods = new HashMap<>();
    private final Map<String, Runnable> pyDecos = new HashMap<>();
    private final Map<String, Value> scripts = new HashMap<>();
    private List<String> pythonPath = new ArrayList<>();

    public static PyLoader getInstance() {
        return INSTANCE;
    }

    public void register(String id, Runnable runnable) {
        pyDecos.put(id, runnable);
    }

    public void init(Path path, Context context) throws IOException {
        try (var list = Files.list(path)) {
            list.filter(p -> p.toString().endsWith(".pyz")).forEach(p -> {
                try {
                    extractPyz(p);

                    Path modRoot = GamePlatform.get().getGameDir().resolve("mods/python/" + p.getFileName());
                    pythonPath.add(modRoot.toAbsolutePath().toString());
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
            Path resolve = modRoot.resolve(pyMod.id() + "/__init__.py");
            if (!Files.exists(resolve)) continue;

            Context context = Context.newBuilder("python")
                    .allowAllAccess(true)
                    .sandbox(SandboxPolicy.TRUSTED)
                    .option("python.EmulateJython", "true")
                    .option("python.PythonPath", String.join(File.pathSeparator, this.pythonPath))
                    .useSystemExit(false)
                    .currentWorkingDirectory(modRoot.toAbsolutePath())
                    .build();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    context.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to close python context", e);
                }
            }));

            try {
                Source source = Source.newBuilder("python", resolve.toFile())
                        .mimeType("text/x-python")
                        .build();
                Value parse = context.parse(source);
                scripts.put(pyMod.id(), parse.execute());

                Runnable runnable = pyDecos.get(pyMod.id());
                if (runnable != null)
                    runnable.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void execute(Path modRoot) {
        Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .sandbox(SandboxPolicy.TRUSTED)
                .option("python.EmulateJython", "true")
                .option("python.PythonPath", modRoot.toAbsolutePath().toString())
                .useSystemExit(false)
                .currentWorkingDirectory(modRoot.toAbsolutePath())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                context.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close python context", e);
            }
        }));

        try {
            List<Path> list = Files.list(modRoot).collect(Collectors.toList());
            for (Path p : list) {
                if (p.toString().endsWith(".py")) {
                    Source source = Source.newBuilder("python", p.toFile())
                            .name(p.getFileName().toString())
                            .mimeType("text/x-python")
                            .build();

                    LOGGER.info("Loading: " + p.getFileName());
                    context.parse(source);
                }
            }

            Path preLaunchScript = modRoot.resolve("__pre_launch__.py");
            if (!Files.exists(preLaunchScript)) return;
            Value python = context.parse(Source.newBuilder("python", preLaunchScript.toFile()).mimeType("text/x-python").build());
            python.execute();
        } catch (Exception e) {
            PyLang.LOGGER.error("Failed to load python file: {}", modRoot, e);
        }
    }

    private void extractPyz(Path p) throws IOException {
        Path resolve = GamePlatform.get().getGameDir().resolve("mods/python/" + p.getFileName());
        if (Files.exists(resolve)) {
            try {
                Files.walk(resolve).sorted().map(Path::toFile).forEach(File::delete);
            } catch (IOException e) {
                PyLang.LOGGER.error("Failed to delete python file: {}", p, e);
            }
        }
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
                if (entry.isDirectory()) {
                    try {
                        Files.createDirectories(resolve.resolve(entry.getName()));
                    } catch (IOException e) {
                        PyLang.LOGGER.error("Failed to load python file: {}", p, e);
                    }
                } else {
                    try {
                        Files.copy(zipFile.getInputStream(entry), GamePlatform.get().getGameDir().resolve("mods/python/" + p.getFileName() + "/" + entry.getName()));
                    } catch (IOException e) {
                        PyLang.LOGGER.error("Failed to load python file: {}", p, e);
                    }
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
}
