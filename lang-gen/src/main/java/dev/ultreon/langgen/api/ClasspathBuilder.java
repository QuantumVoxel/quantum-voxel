package dev.ultreon.langgen.api;

import dev.ultreon.langgen.LangGenConfig;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ClasspathBuilder extends ClasspathWrapper implements NameTransformer {
    private final List<Class<?>> classes = new ArrayList<>();
    private final boolean ignorePrivate;

    private static int prepareMax;
    private static int max;

    private static int busy = 0;

    ExecutorService executor = Executors.newFixedThreadPool(16);

    protected ClasspathBuilder(boolean ignorePrivate) {
        this.ignorePrivate = ignorePrivate;
    }

    @Override
    protected final Thread doBuild(Path outputDir, Runnable run)  {
        busy++;

        Thread builder = new Thread(() -> {
            createOutputDir(outputDir);

            if (!Files.isDirectory(outputDir))
                throw new RuntimeException("Output path is not a directory: " + outputDir);

            List<URL> urls = new ArrayList<>();

            // Scan .jmod files in the JVM
            Path javaHome = Paths.get(System.getProperty("java.home"));
            Path jmodsDir = javaHome.resolve("jmods");
            if (Files.exists(jmodsDir)) visitJMods(jmodsDir, urls);

            Path libsDir = Paths.get("libs");
            if (Files.exists(libsDir)) visitLibs(libsDir, urls);

            String classPath = System.getProperty("java.class.path");
            if (classPath != null) visitClasspath(classPath, urls);

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(Set.copyOf(urls))
                    .setScanners(Scanners.SubTypes.filterResultsBy((arg0) -> true)));

            Set<String> allClasses = new HashSet<>(reflections.getAll(Scanners.SubTypes));

            if (!allClasses.contains("org.reflections.Reflections"))
                throw new NoClassDefFoundError("org.reflections.Reflections");
            if (!allClasses.contains("java.util.UUID"))
                throw new NoClassDefFoundError("java.util.UUID");

            processEntries(allClasses);
            processClasses(outputDir);

            executor.close();

            run.run();
            busy--;
        });

        return builder;
    }

    private static void createOutputDir(Path outputDir) {
        if (!Files.exists(outputDir)) {
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                throw new IOError(e);
            }
        }
    }

    private void processClasses(Path output) {
        int total = classes.size();
        max += total;
        AtomicInteger count = new AtomicInteger();
        for (Class<?> clazz : classes) {
            CompletableFuture.runAsync(() -> {
                if (LangGenConfig.progressListener != null) {
                    LangGenConfig.progressListener.onProgress(count.incrementAndGet(), prepareMax);
                }
                if (!Modifier.isPublic(clazz.getModifiers()) && !Modifier.isProtected(clazz.getModifiers())) return;
                if ((clazz.getModifiers() & 0x0000100) != 0) return;

                if (ClassCompat.isExcluded(clazz)) {
                    getLogger().log(Level.WARNING, "Skipping ignored class: " + clazz.getName());
                    return;
                }

                try {
                    StringBuilder sw = new StringBuilder();
                    String result = this.visitClass(output, clazz, sw);
                    if (result == null) return;
                    this.writeFile(output, clazz.getName(), result);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }
    }

    private void processEntries(Set<String> allClasses) {
        prepareMax = allClasses.size();

        int processed = 0;
        for (String className : allClasses) {
            if (LangGenConfig.progressListener != null) {
                LangGenConfig.progressListener.onPreprocessProgress(processed, prepareMax);
            }

            try {
                processEntry(className);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // ignore
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to process class: {}" + className, e);
            }

            processed++;
        }
    }

    private static void visitClasspath(String classPath, List<URL> urls) {
        for (String path : classPath.split(File.pathSeparator)) {
            if (!path.endsWith(".jar") && !path.endsWith(".jmod") && !path.endsWith(".zip")) continue;
            try {
                urls.add(new File(path).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new IOError(e);
            }
        }
    }

    private static void visitLibs(Path libsDir, List<URL> urls) {
        try {
            Files.walkFileTree(libsDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".jar")) {
                        urls.add(file.toUri().toURL());
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private static void visitJMods(Path root, List<URL> urls) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".jmod")) {
                        try (FileSystem fs = FileSystems.newFileSystem(file, Collections.emptyMap())) {
                            urls.add(fs.getPath("/").toUri().toURL());
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    protected abstract void writeFile(Path output, String className, String result) throws IOException;

    protected abstract @Nullable String visitClass(Path output, Class<?> clazz, StringBuilder classBuilders);

    protected abstract Logger getLogger();

    private void processEntry(String className) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = Class.forName(className, false, classLoader);

        if (this.ignorePrivate && !Modifier.isPublic(clazz.getModifiers()) && !Modifier.isProtected(clazz.getModifiers())) {
            return;
        }

        if (PackageExclusions.isExcluded(clazz)) {
            return;
        }

        this.classes.add(clazz);
    }
}
