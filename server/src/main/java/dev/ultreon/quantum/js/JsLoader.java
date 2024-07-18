package dev.ultreon.quantum.js;

import com.google.gson.Gson;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.LangLoader;
import dev.ultreon.quantum.graal.GraalLanguages;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.log.LoggerFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.FileSystem;
import org.graalvm.polyglot.io.IOAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class JsLoader implements LangLoader {
    private static final JsLoader INSTANCE = new JsLoader();
    private static final Logger LOGGER = LoggerFactory.getLogger(JsLoader.class);
    public static final String CONTENT_TYPE = "application/javascript+module";
    public static final String LOAD_FAIL_MESSAGE = "Failed to load javascript file: {}";
    public static final IOAccess IO_ACCESS = IOAccess.newBuilder().fileSystem(new JavascriptModuleFS()).allowHostSocketAccess(true).build();
    private final Map<String, JsMod> mods = new HashMap<>();
    private final Map<String, Runnable> jsDecos = new HashMap<>();
    private final Map<String, Value> scripts = new HashMap<>();
    private final List<String> javascriptPath = new ArrayList<>();
    private Map<String, Path> paths = new LinkedHashMap<>();

    public static JsLoader getInstance() {
        return INSTANCE;
    }

    public void register(String id, Runnable runnable) {
        jsDecos.put(id, runnable);
    }

    @Override
    public void init(Path path, Context context) throws IOException {
        Path jsModsPath = path.resolve("js");
        if (Files.notExists(jsModsPath)) {
            LOGGER.warn("Javascript mods folder not found: {}", jsModsPath);
            Files.createDirectories(jsModsPath);
            return;
        }

        LOGGER.debug("Loading Javascript mods from " + jsModsPath);

        try (var list = Files.list(jsModsPath)) {
            list.filter(Files::isRegularFile).forEach(p -> {
                if (p.toString().endsWith(".tgz")) {
                    LOGGER.debug("Attempting to extract Node.JS package: " + p);
                    Path modRoot = null;
                    try {
                        modRoot = extractTgz(p);
                        Path resolve1 = path.getParent().resolve("mods-out");
                        if (Files.notExists(resolve1)) {
                            Files.createDirectories(resolve1);
                        }
                        Files.move(p, resolve1.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

                        Path resolved = modRoot.resolve("package.json");
                        JsMod jsMod = new Gson().fromJson(Files.readString(resolved), JsMod.class);
                        if (!Pattern.matches("[a-z0-9_]+", jsMod.name)) {
                            throw new IllegalArgumentException("Invalid mod id!");
                        }
                        Path oldRoot = modRoot;
                        modRoot = modRoot.getParent().resolve(jsMod.name);
                        if (!oldRoot.equals(modRoot)) {
                            Files.move(oldRoot, modRoot, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                        }
                    } catch (IOException e) {
                        JsLang.LOGGER.error("Failed to extract Node.JS package!", e);
                        if (modRoot != null) {
                            try {
                                FileUtils.deleteDirectory(modRoot.toFile());
                            } catch (IOException ex) {
                                JsLang.LOGGER.error("Failed to delete the failed import!", ex);
                            }
                        }
                    }
                } else {
                    LOGGER.warn("Not a Node.JS package: " + p);
                }
            });
        }
        try (var list = Files.list(jsModsPath)) {
            list.filter(Files::isDirectory).forEach(p -> {
                LOGGER.debug("Attempting to load Node.JS package: " + p.getFileName());
                Path modRoot = path.resolve("js").resolve(p.getFileName());
                Path resolved = modRoot.resolve("package.json");

                if (Files.notExists(resolved)) {
                    LOGGER.warn("Not a Node.JS package: " + p.getFileName() + ". The file " + resolved + " is not present.");
                    return;
                }
                try {
                    JsMod jsMod = new Gson().fromJson(Files.readString(resolved), JsMod.class);
                    if (!Pattern.matches("[a-z0-9_]+", jsMod.name)) {
                        throw new IllegalArgumentException("Invalid mod id!");
                    }
                    Path oldRoot = modRoot;
                    modRoot = modRoot.getParent().resolve(jsMod.name);
                    if (!oldRoot.equals(modRoot)) {
                        Files.move(oldRoot, modRoot, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    }
                    javascriptPath.add(jsModsPath.toString());
                    paths.put(jsMod.name, modRoot);
                    mods.put(jsMod.id(), jsMod);
                    jsMod.path = modRoot;

                    execute(modRoot, jsMod);
                } catch (Exception e) {
                    JsLang.LOGGER.error(LOAD_FAIL_MESSAGE, p, e);
                }
            });
        }
    }

    public void initMods() {
        for (JsMod jsMod : mods.values()) {
            Path modRoot = jsMod.path;
            Path resolve = modRoot.resolve("index.mjs");
            if (!Files.exists(resolve)) {
                LOGGER.debug("JS Mod does not have index.mjs: " + jsMod.id() + ". Searched at " + resolve);
                continue;
            }

            try {
                Source source = Source.newBuilder("js", resolve.toFile())
                        .mimeType(CONTENT_TYPE)
                        .build();
                Value parse = GraalLanguages.context.parse(source);
//                scripts.put(jsMod.id(), parse.execute());

                Runnable runnable = jsDecos.get(jsMod.id());
                if (runnable != null)
                    runnable.run();
            } catch (IOException e) {
                throw new ModLoadException(e);
            }
        }
    }

    private void execute(Path modRoot, JsMod mod) {
        try(Stream<Path> list1 = Files.list(modRoot)) {
            List<Path> list = list1.toList();
            for (Path p : list) {
                if (p.toString().endsWith(".mjs")) {
                    LOGGER.info("Loading: " + p.getFileName());
                }
            }

            if (Files.exists(modRoot.resolve("index.mjs"))) {
                Source source = Source.newBuilder("js", """
                                import main from '%1$s/index.mjs';
                                
                                main();
                                """.formatted(mod.name), "<<DynModInit-%s>>".formatted(mod.name))
                        .mimeType(CONTENT_TYPE)
                        .build();

                GraalLanguages.context.parse(source).executeVoid();
            }
            Path preLaunchScript = modRoot.resolve("pre-launch.mjs");
            if (!Files.exists(preLaunchScript)) return;
            Value javascript = GraalLanguages.context.parse(Source.newBuilder("js", preLaunchScript.toFile()).mimeType(CONTENT_TYPE).build());
            javascript.execute();
        } catch (Exception e) {
            JsLang.LOGGER.error(LOAD_FAIL_MESSAGE, modRoot, e);
        }
    }

    public static void extractTgz(String tgzFilePath, String destDir) throws IOException {
        try (InputStream fileInputStream = new FileInputStream(tgzFilePath);
             InputStream gzipInputStream = new GzipCompressorInputStream(fileInputStream);
             TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

            TarArchiveEntry entry;
            while ((entry = tarInputStream.getNextEntry()) != null) {
                if (!entry.getName().startsWith("package/")) continue;

                String name = entry.getName().substring(8);

                File outputFile = new File(destDir, name);
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        if (!outputFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + outputFile.getAbsolutePath());
                        }
                    }
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.exists()) {
                        if (!parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent.getAbsolutePath());
                        }
                    }
                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = tarInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
    }

    private Path extractTgz(Path p) throws IOException {
        if (!p.toString().endsWith(".tgz")) return p;
        Path resolve = GamePlatform.get().getGameDir().resolve("mods/js/" + p.getFileName());
        String string = resolve.toAbsolutePath().toString();
        String outputDir = string.substring(0, string.length() - 4);
        extractTgz(p.toAbsolutePath().toString(), outputDir);

        return Path.of(outputDir);
    }

    public void close() {

    }

    public Collection<JsMod> getMods() {
        return Collections.unmodifiableCollection(mods.values());
    }

    public Path getModPath(JsMod jsMod) {
        return paths.get(jsMod.getName());
    }

    private static class JavascriptModuleFS implements FileSystem {
        private final FileSystem delegate = FileSystem.newDefaultFileSystem();

        @Override
        public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
            delegate.setAttribute(path, attribute, value, options);
        }

        @Override
        public void copy(Path source, Path target, CopyOption... options) throws IOException {
            delegate.copy(source, target, options);
        }

        @Override
        public void move(Path source, Path target, CopyOption... options) throws IOException {
            delegate.move(source, target, options);
        }

        @Override
        public void createLink(Path link, Path existing) throws IOException {
            delegate.createLink(link, existing);
        }

        @Override
        public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
            delegate.createSymbolicLink(link, target, attrs);
        }

        @Override
        public Path readSymbolicLink(Path link) throws IOException {
            return delegate.readSymbolicLink(link);
        }

        @Override
        public void setCurrentWorkingDirectory(Path currentWorkingDirectory) {
            delegate.setCurrentWorkingDirectory(currentWorkingDirectory);
        }

        @Override
        public String getSeparator() {
            return delegate.getSeparator();
        }

        @Override
        public String getPathSeparator() {
            return delegate.getPathSeparator();
        }

        @Override
        public String getMimeType(Path path) {
            if (path.toString().endsWith(".mjs")) return CONTENT_TYPE;
            else if (path.toString().endsWith(".py")) return "text/x-python";
            else if (path.toString().endsWith(".wasm")) return "application/wasm";
            else return "text/plain";
        }

        @Override
        public Charset getEncoding(Path path) {
            return delegate.getEncoding(path);
        }

        @Override
        public Path getTempDirectory() {
            return delegate.getTempDirectory();
        }

        @Override
        public boolean isSameFile(Path path1, Path path2, LinkOption... options) throws IOException {
            return delegate.isSameFile(path1, path2, options);
        }

        @Override
        public Path parsePath(URI uri) {
            return delegate.parsePath(uri);
        }

        @Override
        public Path parsePath(String path) {
            return delegate.parsePath(path);
        }

        @Override
        public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
            delegate.checkAccess(path, modes, linkOptions);
        }

        @Override
        public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
            delegate.createDirectory(dir, attrs);
        }

        @Override
        public void delete(Path path) throws IOException {
            delegate.delete(path);
        }

        @Override
        public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
            return delegate.newByteChannel(path, options, attrs);
        }

        @Override
        public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
            return delegate.newDirectoryStream(dir, filter);
        }

        @Override
        public Path toAbsolutePath(Path path) {
            return delegate.toAbsolutePath(path);
        }

        @Override
        public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
            return delegate.toRealPath(path, linkOptions);
        }

        @Override
        public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
            return delegate.readAttributes(path, attributes, options);
        }
    }
}
